<template>


  <a-modal class="imageCropper" v-model:visible="visible" title="EditPicture" :footer="false" @cancel="closeModal">
    <!--    图片裁切组件-->
    <vue-cropper
      ref="cropperRef"
      :img="imageUrl"
      :autoCrop="true"
      :fixedBox="false"
      :centerBox="true"
      :canMoveBox="true"
      :info="true"
      outputType="png"
    />
    <div style="margin-bottom: 16px" />
    <!-- 图片操作 -->
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="rotateLeft">RotateLeft</a-button>
        <a-button @click="rotateRight">RotateRight</a-button>
        <a-button @click="changeScale(1)">ScaleUp</a-button>
        <a-button @click="changeScale(-1)">ScaleDown</a-button>
        <a-button type="primary" :loading="loading" @click="handleConfirm">Confirm</a-button>
      </a-space>
    </div>
  </a-modal>


</template>

<script setup lang="ts">


import { ref } from 'vue'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  imageUrl?: string
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}


const loading = ref(false)

const props = defineProps<Props>()

// 获取图片裁切器的引用
const cropperRef = ref()

// 缩放图片
const changeScale = (num: number) => {
  cropperRef.value.changeScale(num)
}

// 左右旋转
const rotateLeft = () => {
  cropperRef.value.rotateLeft()
}
const rotateRight = () => {
  cropperRef.value.rotateRight()
}

// 确认裁剪
const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    // blob 为已裁切的文件
    const filename = (props.picture?.name || 'picture') + '.png'
    const file = new File([blob], filename, { type: 'image/png' })
    handleUpload({ file })
  })
}



/**
 * 上传
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    // 检查GIF文件的处理
    if (file.type === 'image/gif') {
      // 确保GIF文件名有.gif后缀
      const fileName = file.name
      if (!fileName.toLowerCase().endsWith('.gif')) {
        // 创建新的File对象，修改文件名
        const renamedFile = new File([file], `${fileName}.gif`, { type: file.type })
        file = renamedFile
        console.log('Renamed GIF file to ensure .gif extension:', file.name)
      }
    }

    const params:API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('Picture upload success')
      // 将上传成功的图片信息传递给父组件
      // 上传成功后调用父组件传递的 onSuccess 回调，通知父组件更新 picture 数据
      props.onSuccess?.(res.data.data) // 调用父组件的 onSuccess 函数，传递上传的数据,这个时候就把数据传递给父组件了，而父组件的picture又是响应式的
      // 关闭弹窗
      closeModal();
    } else {
      message.error('Picture upload failed，' + (res.data.message || ''))
    }
  } catch (error) {
    console.error('Upload error:', error)
    message.error('Picture upload failed')
  } finally {
    loading.value = false
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
.imageCropper {
  text-align: center;
}

.imageCropper .vue-cropper {
  height: 400px !important;
}
</style>
