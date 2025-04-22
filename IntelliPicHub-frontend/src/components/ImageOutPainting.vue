<template>
  <a-modal
    class="imageOutPainting"
    v-model:visible="visible"
    title="AI OutPainting" :footer="false"
    @cancel="closeModal"
  >
    <a-row gutter="16">
      <a-col span="12">
        <h4>Original</h4>
        <img :src="picture?.url" alt="picture?.name" style="max-width: 100%" />
      </a-col>

      <a-col span="12">
        <h4>OutPainting</h4>
        <img
          v-if="resultImageUrl"
          :src="resultImageUrl"
          alt="picture?.name"
          style="max-width: 100%" />
      </a-col>
    </a-row>
    <div style="margin-bottom: 16px"></div>
    <a-flex justify="center" gap="16">
      <a-button type="primary" :loading="!!taskId"  ghost @click="createTask">Generate</a-button>
      <a-button v-if="resultImageUrl" :loading="uploadLoading" type="primary" @click="handleUpload">Apply</a-button>
    </a-flex>

    <div style="margin-bottom: 16px" />

  </a-modal>


</template>

<script setup lang="ts">


import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  createPictureOutPaintingTaskUsingPost,
  getPictureOutPaintingTaskUsingGet, uploadPictureByUrlUsingPost
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

// 使用更合适的方式定义定时器类型
type TimeoutHandle = ReturnType<typeof setTimeout>

interface Props {
  picture?: API.PictureVO  // PictureVO 应该包含 id, name, introduction, category, tags 等属性
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}


const props = defineProps<Props>()
const router = useRouter()

const resultImageUrl = ref<string>('')

const taskId = ref<string>()

// 是否正在上传
const uploadLoading = ref(false)

/**
 * 创建任务
 */
const createTask = async () => {
  if (!props.picture?.id) {
    return
  }
  const res = await createPictureOutPaintingTaskUsingPost({
    pictureId: props.picture.id,
    // 根据需要设置扩图参数
    parameters: {
      xScale: 2,
      yScale: 2
    }
  })
  if (res.data.code === 0 && res.data.data && res.data.data.output) {
    message.success('Task created successfully, please wait patiently and do not exit the interface')
    console.log(res.data.data.output.taskId)
    taskId.value = res.data.data.output.taskId
    // 开启轮询
    startPolling()
    // 将上传成功的图片信息传递给父组件
  } else {
    message.error('Failed to create outpainting task,' + res.data.message)
  }
}

// 轮询定时器
let pollingTimer: TimeoutHandle | null = null


// 开始轮询
const startPolling = () => {
  if (!taskId.value) {
    return
  }

  pollingTimer = setInterval(async () => {
    try {
      const res = await getPictureOutPaintingTaskUsingGet({
        taskId: taskId.value
      })
      if (res.data.code === 0 && res.data.data) {
        const taskResult = res.data.data.output as {
          taskStatus: string;
          outputImageUrl?: string;
          code?: string;
          message?: string;
        }
        if (taskResult.taskStatus === 'SUCCEEDED') {
          message.success('Outpainting success')
          if (taskResult.outputImageUrl) {
            resultImageUrl.value = taskResult.outputImageUrl
          }
          // 清理轮询
          clearPolling()
        } else if (taskResult.taskStatus === 'FAILED') {
          // 获取详细的错误信息
          const errorCode = taskResult.code || '';
          const errorMessage = taskResult.message || '';

          // 根据错误代码提供友好的错误提示
          let friendlyMessage = 'Failed to outpainting';

          if (errorCode === 'InvalidParameter.ImageResolution') {
            friendlyMessage = 'Image resolution is not suitable for outpainting. The image resolution must be between 512*512 and 4096*4096 pixels.';
          } else if (errorMessage) {
            // 如果有错误消息但没有特定处理，直接显示错误消息
            friendlyMessage = `Failed to outpainting: ${errorMessage}`;
          }

          // 在控制台记录完整错误信息，便于调试
          console.error('Outpainting failed:', { errorCode, errorMessage });

          // 显示友好的错误提示
          message.error(friendlyMessage);

          // 清理轮询
          clearPolling()
        }
      }
    } catch (error: unknown) {
      console.error('Failed to poll,', error)
      const errorMessage = error instanceof Error ? error.message : 'Unknown error'
      message.error('Failed to poll, ' + errorMessage)
      // 清理轮询
      clearPolling()
    }
  }, 3000) as unknown as TimeoutHandle // 转换类型以匹配
}

// 清理轮询
const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = undefined
  }
}


/**
 * url上传
 */
const handleUpload = async () => {
  if (!resultImageUrl.value) {
    message.error('No outpainting result available')
    return
  }

  uploadLoading.value = true
  try {
    const params: API.PictureUploadRequest = {
      fileUrl: resultImageUrl.value,
      spaceId: props.spaceId
    }

    // 保留原图的信息（名称、简介、分类、标签）
    if (props.picture) {
      // 如果是更新同一张图片而不是创建新图片，则传递原图ID
      // params.id = props.picture.id  // 注释掉这行，因为我们需要创建新图片而不是覆盖原图

      // 保留原图的名称（添加扩图标识）
      params.picName = props.picture.name ? `${props.picture.name} (Outpainted)` : undefined

      // 保留原图的简介
      params.introduction = props.picture.introduction

      // 保留原图的分类
      params.category = props.picture.category

      // 保留原图的标签
      params.tags = props.picture.tags
    }

    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('Picture upload success')

      // 获取新创建的图片ID
      const newPictureId = res.data.data.id

      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)

      // 关闭模态框
      closeModal()

      // 跳转到新图片的详情页
      if (newPictureId) {
        router.push(`/picture/${newPictureId}`)
      }
    } else {
      message.error('Picture upload fail，' + res.data.message)
    }
  } catch (error: unknown) {
    console.error('Picture upload failed:', error)
    message.error('Picture upload fail')
  } finally {
    uploadLoading.value = false
  }
}


// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal
})


</script>

<style>
.imageOutPainting {
  text-align: center;
}

</style>
