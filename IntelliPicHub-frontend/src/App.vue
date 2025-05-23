<template>
  <div id="app">
<!--    <a-config-provider :locale="currentLocale">-->
    <a-config-provider :locale="enUS">

    <BasicLayout />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import jaJP from 'ant-design-vue/es/locale/ja_JP'
import enUS from 'ant-design-vue/es/locale/en_US'
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import 'dayjs/locale/en'
import 'dayjs/locale/ja'

/**
 * 获取当前用户首选语言
 * @returns {string} 返回用户首选语言代码
 */
const getUserLanguage = (): string => {
  // 浏览器语言，如 'zh-CN', 'en-US', 'ja-JP'
  const browserLang = navigator.language || (navigator as Navigator & { userLanguage?: string }).userLanguage || 'en'
  return browserLang.toLowerCase()
}

/**
 * 根据用户语言选择相应的locale
 * @param {string} userLang - 用户语言代码
 * @returns {object} 对应的locale对象
 */
const getLocaleByLanguage = (userLang: string) => {
  if (userLang.startsWith('zh')) {
    dayjs.locale('zh-cn')
    return zhCN
  } else if (userLang.startsWith('ja')) {
    dayjs.locale('ja')
    return jaJP
  } else {
    // 默认英文
    dayjs.locale('en')
    return enUS
  }
}

const currentLocale = ref(enUS) // 默认中文

onMounted(() => {
  const userLang = getUserLanguage()
  currentLocale.value = getLocaleByLanguage(userLang)
})
</script>


<style scoped>

</style>
