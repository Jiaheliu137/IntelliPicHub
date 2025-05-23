<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo2.png" alt="logo" />
            <div class="title">IntelliPicHub</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="menus"
          @click="doMenuClick"
        />
      </a-col>

      <!--      用户信息展示栏-->
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? 'anonymous' }}
              </a-space>

              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <router-link to="/my_space">
                      <UserOutlined />
                      My Space
                    </router-link>
                  </a-menu-item>
                  <a-menu-item>
                    <router-link to="/user/profile">
                      <SettingOutlined />
                      Settings
                    </router-link>
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    Logout
                  </a-menu-item>

                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login"> Login</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { HomeOutlined, LogoutOutlined, UserOutlined, SettingOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter, type RouteRecordRaw } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'
import checkAccess from '@/access/checkAccess.ts'

const loginUserStore = useLoginUserStore()

const router = useRouter() //get the current router instance

// 未经过滤的菜单项
const originMenus = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: 'Home',
    title: 'Home'
  },
  {
    key: '/add_picture',
    label: 'Add',
    title: 'Add picture'
  },
  // {
  //   key: '/vip/exchange',
  //   icon: () => h(StarOutlined),
  //   label: 'VIP Membership',
  //   title: 'Activate VIP'
  // },
  {
    key: '/admin/userManage',
    label: 'UserManage',
    title: 'admin user manage'
  },
  {
    key: '/admin/pictureManage',
    label: 'PictureManage',
    title: 'admin picture manage'
  },
  {
    key: '/admin/spaceManage',
    label: 'SpaceManage',
    title: 'admin space manage'
  }
  // {
  //   key: 'others',
  //   label: h('a', { href: 'https://github.com/Jiaheliu137', target: '_blank' }, 'GitHub'),
  //   title: 'github'
  // }
]

/**
 * @description 将菜单项转换为路由项
 * @param {any} menu - 菜单项
 * @returns {RouteRecordRaw} 与菜单项key对应的路由项
 */
const menuToRouteItem = (menu: any): RouteRecordRaw => {
  // 获取所有路由
  const routes = router.getRoutes()
  // 根据菜单的key查找对应的路由
  const route = routes.find((route) => route.path === menu?.key)
  // 如果找到对应路由则返回，否则返回一个默认的空路由对象
  return route || ({} as RouteRecordRaw)
}

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  // 过滤条件是一个回调函数 (menu) => { ... }, 返回 true 表示保留该菜单项，返回 false 表示过滤掉该菜单项
  return menus?.filter((menu) => {
    // 通过menu的key值找到对应的路由字段
    const item = menuToRouteItem(menu)

    // 如果是菜单项中没有对应的路由，就说明该菜单是自定义的，予以保留，返回true
    if (!item.path) {
      return true
    }

    // 如果有hideInMenu标记为true，则隐藏
    if (item.meta?.hideInMenu) {
      return false
    }

    // 根据权限过滤菜单，有权限则返回true，会保留该菜单
    return checkAccess(loginUserStore.loginUser, item.meta?.access as string)
  })
}

// 展示在菜单的路由数组
const menus = computed(() => {
  return filterMenus(originMenus)
})

// 当前要高亮的菜单项
const current = ref<string[]>()
// 监听路由变化，更新高亮菜单项
router.afterEach((to) => {
  current.value = [to.path]
})

// 路由跳转事件 Routing Transition Event
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key
  })
}

// User Logout
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: 'unLogin'
    })
    message.success('Logout successfully')
    await router.push('/user/login')
  } else {
    message.error('Logout failed ' + res.data.message)
  }
}
</script>

<style scoped>
#globalHeader {
  height: 100%;
}

#globalHeader .title-bar {
  display: flex;
  align-items: center;
  height: 100%;
}

.logo {
  height: 48px;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

/* 导航菜单样式 */
:deep(.ant-menu) {
  background: transparent !important;
  border-bottom: none !important;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64px;
  flex: 1;
}

:deep(.ant-menu-item) {
  margin: 0 6px !important;
  padding: 0 20px !important;
  border-radius: 8px !important;
  height: 42px !important;
  line-height: 42px !important;
  background: rgba(255, 255, 255, 0.4) !important;
  backdrop-filter: blur(4px) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  color: rgba(0, 0, 0, 0.85) !important;
}

:deep(.ant-menu-item)::after {
  display: none !important;
}

:deep(.ant-menu-item:hover) {
  background: rgba(255, 255, 255, 0.6) !important;
  border-color: rgba(24, 144, 255, 0.3) !important;
  color: #1890ff !important;
  transform: translateY(-1px);
}

:deep(.ant-menu-item-selected) {
  background: rgba(230, 244, 255, 0.8) !important;
  border-color: rgba(24, 144, 255, 0.5) !important;
  color: #1890ff !important;
  font-weight: 600 !important;
}

/* 用户信息区域样式 */
.user-login-status {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

/* 调整行布局 */
:deep(.ant-row) {
  height: 100%;
  align-items: center;
}
</style>
