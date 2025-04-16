<template>
  <div class="urlPictureUpload">

    <a-input-group compact style="margin-bottom: 16px">
      <a-input v-model:value="fileUrl" style="width: calc(100% - 120px)" placeholder="Input picture URL" />
      <a-button type="primary" :loading="loading" @click="handleUpload" style="width: 120px">Submit</a-button>
    </a-input-group>

  <div class="image-wrapper">
    <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
  </div>


  </div>

</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost, uploadPictureUsingPost } from '@/api/pictureController.ts'


// 子组件定义的 props（属性）是供 父组件 向子组件传递数据的机制
// 接收父组件传递的 props
interface Props {
  spaceId?: number
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()
const loading = ref<boolean>(false)
const fileUrl = ref<string>()

/**
 * 上传
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = { fileUrl: fileUrl.value }
    params.spaceId = props.spaceId
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('Picture upload success')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('Picture upload fail，' + res.data.message)
    }
  } catch (error) {
    message.error('Picture upload fail')
  } finally {
    loading.value = false
  }
}





</script>
<style scoped>

.urlPictureUpload {
  margin-bottom: 16px;

}


.urlPictureUpload img {
  max-width: 100%;
  max-height: 480px;
}

.urlPictureUpload .image-wrapper{
  text-align: center;
  margin-top: 16px;
}

</style>
