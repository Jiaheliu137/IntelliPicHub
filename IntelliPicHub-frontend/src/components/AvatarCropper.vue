<template>
  <a-modal class="avatarCropper" v-model:visible="visible" title="Crop Avatar" :footer="false" @cancel="closeModal">
    <!-- 图片裁切组件 -->
    <vue-cropper
      ref="cropperRef"
      :img="imageUrl"
      :autoCrop="true"
      :fixedBox="true"
      :centerBox="true"
      :canMoveBox="true"
      :info="true"
      outputType="png"
      :fixed="true"
      :fixedNumber="[1, 1]"
    />
    <div style="margin-bottom: 16px" />

    <!-- 图片操作 -->
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="rotateLeft">Rotate Left</a-button>
        <a-button @click="rotateRight">Rotate Right</a-button>
        <a-button @click="changeScale(1)">Zoom In</a-button>
        <a-button @click="changeScale(-1)">Zoom Out</a-button>
        <a-button type="primary" :loading="loading" @click="handleConfirm">Confirm</a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { message } from 'ant-design-vue'
import { uploadAvatarUsingPost } from '@/api/userController'

interface Props {
  imageUrl?: string
}

const props = defineProps<Props>()
const emit = defineEmits(['success'])

const loading = ref(false)
const visible = ref(false)

// 监听imageUrl变化
watch(() => props.imageUrl, (newValue) => {
  console.log('Avatar image URL changed:', newValue)
})

// 获取图片裁切器的引用
const cropperRef = ref()

// 向左旋转
const rotateLeft = () => {
  cropperRef.value.rotateLeft()
}

// 向右旋转
const rotateRight = () => {
  cropperRef.value.rotateRight()
}

// 缩放
const changeScale = (num: number) => {
  cropperRef.value.changeScale(num)
}

// 确认裁剪
const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    // blob 为已裁切的文件
    const filename = 'avatar.png'
    const file = new File([blob], filename, { type: 'image/png' })
    handleUpload(file)
  })
}

/**
 * 上传头像
 * @param file 裁剪后的文件
 */
const handleUpload = async (file: File) => {
  loading.value = true
  try {
    // 上传头像文件
    const uploadRes = await uploadAvatarUsingPost({}, file)
    if (uploadRes.data.code === 0 && uploadRes.data.data) {
      // 上传成功，通知父组件
      emit('success', { url: uploadRes.data.data })
      // 关闭弹窗
      closeModal()
    } else {
      message.error(uploadRes.data.message || 'Upload failed')
    }
  } catch (error) {
    console.error('Avatar upload failed', error)
    message.error('Avatar upload failed')
  } finally {
    loading.value = false
  }
}

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
.avatarCropper {
  text-align: center;
}

.avatarCropper .vue-cropper {
  height: 400px !important;
}

.image-cropper-actions {
  margin-top: 16px;
  text-align: center;
}
</style>
