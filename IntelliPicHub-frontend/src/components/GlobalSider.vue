<template>
  <div id="globalSider">
    <a-layout-sider
      v-if="loginUserStore.loginUser.id"
      class="sider" width="200"
      breakpoint="lg"
      collapsed-width="0"
    >
      <a-menu
        mode="inline"
        v-model:selectedKeys="current"
        :items="menuItems"
        @click="doMenuClick"
      />
    </a-layout-sider>
  </div>


</template>
<script lang="ts" setup>
import { h, ref } from 'vue'

import { useRouter } from 'vue-router'
import { PictureOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

const loginUserStore = useLoginUserStore()

const menuItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: 'Public picture'
  },
  {
    key: '/my_space',
    icon: () => h(UserOutlined),
    label: 'My space'
  }
]

const router = useRouter() //get the current router instance
// 当前要高亮的菜单项
const current = ref<string[]>()
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 路由跳转事件 Routing Transition Event
const doMenuClick = ({ key }) => {
  router.push({
    path: key
  })
}

</script>

<style scoped>
#globalSider .ant-layout-sider {
  background: none;
}


</style>
