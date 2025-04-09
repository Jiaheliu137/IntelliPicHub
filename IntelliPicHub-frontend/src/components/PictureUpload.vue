<template>
  <div class="pictureUpload">
    <a-upload

      list-type="picture-card"

      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"

    >
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">Click or drag to upload</div>
      </div>
    </a-upload>
  </div>

</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'


// 子组件定义的 props（属性）是供 父组件 向子组件传递数据的机制
// 接收父组件传递的 props
interface Props {
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()



const loading = ref<boolean>(false)

/**
 * 上传
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params = props.picture ? { id: props.picture.id } : {};
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('Picture upload success')
      // 将上传成功的图片信息传递给父组件
      // 上传成功后调用父组件传递的 onSuccess 回调，通知父组件更新 picture 数据
      props.onSuccess?.(res.data.data) // 调用父组件的 onSuccess 函数，传递上传的数据,这个时候就把数据传递给父组件了，而父组件的picture又是响应式的
    } else {
      message.error('Picture upload failed，' + res.data.message)
    }
  } catch (error) {
    message.error('Picture upload failed')
  } finally {
    loading.value = false
  }
}




/**
 * 上传前的校验（前端校验）
 * @param file
 */
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  // 校验图片格式
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('Don not support this format，recommend jpg，jpeg，png')
  }
  // 校验图片大小
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('Image must smaller than 2MB!')
  }
  return isJpgOrPng && isLt2M
}
</script>
<style scoped>

.pictureUpload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-width: 152px;
  min-height: 152px;
}


.pictureUpload img{
  max-width: 100%;
  max-height: 480px;
}



.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
</style>
