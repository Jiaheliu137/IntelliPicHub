<template>
  <div>
    <a-modal v-model:visible="visible" :title="title" :footer="false" @cancel="closeModal">
      <h4>Copy share link</h4>
      <a-typography-link copyable>
        {{link}}
      </a-typography-link>
      <div style="margin-bottom: 16px"></div>
      <h4>Scan QRCode</h4>
      <a-qrcode :value="props.link"  />



    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'

const visible = ref<boolean>(false)


interface Props {
  title:string;
  link: string;
}

const props=withDefaults(defineProps<Props>(),{
  title: 'Share Picture',
  link: 'https://github.com/Jiaheliu137'
})

// 开启弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal,
})

</script>

