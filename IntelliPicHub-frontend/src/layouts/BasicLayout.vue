<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header" :class="{ 'header-hidden': isHeaderHidden }">
        <GlobalHeader />
      </a-layout-header>

      <a-layout-content class="content">
        <router-view v-slot="{ Component, route }">
          <keep-alive :include="['Home', 'SpaceInformation']">
            <component :is="Component" :key="route.path" v-if="route.meta.keepAlive" />
          </keep-alive>
          <component :is="Component" :key="route.path" v-if="!route.meta.keepAlive" />
        </router-view>
      </a-layout-content>

      <a-layout-footer class="footer" :class="{ 'footer-hidden': !isFooterVisible }">
        <a href="https://github.com/Jiaheliu137/IntelliPicHub" target="_blank">
          <GithubOutlined style="margin-right: 6px;" />
          IntelliPicHub
        </a>
      </a-layout-footer>
    </a-layout>

    <!-- 悬浮侧边栏 -->
    <GlobalSider />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
import { GithubOutlined } from '@ant-design/icons-vue'

// 滚动相关状态
const isHeaderHidden = ref(false)
const isFooterVisible = ref(false)
const lastScrollY = ref(0)
const scrollThreshold = 10 // 滚动阈值，避免小幅滚动时频繁切换

/**
 * @description 检查是否滚动到页面底部
 */
const isScrolledToBottom = () => {
  const scrollHeight = document.documentElement.scrollHeight
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const clientHeight = document.documentElement.clientHeight
  // 当距离底部50px时就显示footer
  return scrollHeight - scrollTop - clientHeight <= 50
}

/**
 * @description 滚动监听函数，控制header和footer的显示隐藏
 */
const handleScroll = () => {
  const currentScrollY = window.scrollY

  // 控制header显示隐藏
  if (currentScrollY <= 0) {
    isHeaderHidden.value = false
    lastScrollY.value = currentScrollY
  } else {
    const scrollDifference = currentScrollY - lastScrollY.value
    if (Math.abs(scrollDifference) > scrollThreshold) {
      isHeaderHidden.value = scrollDifference > 0
      lastScrollY.value = currentScrollY
    }
  }

  // 控制footer显示隐藏
  isFooterVisible.value = isScrolledToBottom()
}

// 组件挂载时添加滚动监听
onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  lastScrollY.value = window.scrollY
  // 初始检查一次footer是否应该显示
  isFooterVisible.value = isScrolledToBottom()
})

// 组件卸载时移除滚动监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(12px);
  color: unset;
  margin-bottom: 1px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

/* header隐藏状态 */
#basicLayout .header.header-hidden {
  transform: translateY(-100%);
}

/* 为固定头部腾出空间 */
#basicLayout .content {
  padding: 28px;
  background: linear-gradient(to right, #fefefe, #fff);
  margin-bottom: 28px;
  margin-top: 64px; /* 为固定头部腾出空间 */
  width: 100%;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
}

#basicLayout .footer {
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(12px);
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.03);
  transition: transform 0.3s ease-in-out;
}

/* footer隐藏状态 */
#basicLayout .footer.footer-hidden {
  transform: translateY(100%);
}
</style>
