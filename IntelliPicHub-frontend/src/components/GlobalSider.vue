<template>
  <div
      v-if="loginUserStore.loginUser.id"
    id="globalSider"
    class="floating-sider"
    :class="{ 'expanded': isExpanded }"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <!-- 触发按钮区域 -->
    <div class="trigger-area">
      <div class="trigger-button">
        <MenuOutlined />
      </div>
    </div>

    <!-- 侧边栏内容 -->
    <div class="sider-content" v-show="isExpanded">
      <div class="sider-header">
        <img class="logo" src="../assets/logo2.png" alt="logo" />
        <span class="title">Guide</span>
      </div>

      <a-menu
        mode="inline"
        v-model:selectedKeys="current"
        :items="menuItems"
        @click="doMenuClick"
        class="floating-menu"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, h, ref, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { PictureOutlined, TeamOutlined, UserOutlined, MenuOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { SPACE_TYPE_ENUM, SPACE_ROLE_ENUM } from '@/constants/space.ts'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController.ts'
import { message } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

// 侧边栏展开状态
const isExpanded = ref(false)

/**
 * @description 鼠标进入事件
 */
const handleMouseEnter = () => {
  isExpanded.value = true
}

/**
 * @description 鼠标离开事件
 */
const handleMouseLeave = () => {
  isExpanded.value = false
}

// 团队空间列表
const teamSpaceList = ref<API.SpaceUserVO[]>([])

/**
 * @description 获取用户拥有的团队空间（角色为admin的团队空间）
 */
const myOwnedTeamSpace = computed(() => {
  return teamSpaceList.value.find(spaceUser =>
    spaceUser.spaceRole === SPACE_ROLE_ENUM.ADMIN
  )
})

/**
 * @description 构建菜单项
 */
const menuItems = computed(() => {
  const baseMenuItems = [
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

  // 我的团队空间菜单项
  const myTeamMenuItem = {
    key: myOwnedTeamSpace.value
      ? `/space/${myOwnedTeamSpace.value.spaceId}`
      : `/add_space?type=${SPACE_TYPE_ENUM.TEAM}`,
    label: 'My team',
    icon: () => h(TeamOutlined),
  }

  // 如果没有拥有的团队空间，只显示基础菜单
  if (teamSpaceList.value.length < 1) {
    return [...baseMenuItems, myTeamMenuItem]
  }

  // 获取参与的其他团队空间（不包括自己拥有的）
  const otherTeamSpaces = teamSpaceList.value.filter(spaceUser =>
    spaceUser.spaceRole !== SPACE_ROLE_ENUM.ADMIN
  )

  // 如果有其他团队空间，创建团队空间分组
  if (otherTeamSpaces.length > 0) {
    const teamSpaceSubMenus = otherTeamSpaces.map((spaceUser) => {
    const space = spaceUser.space
    return {
      key: '/space/' + spaceUser.spaceId,
      label: space?.spaceName,
    }
  })

  const teamSpaceMenuGroup = {
    type: 'group',
      label: 'Joined teams',
      key: 'joinedTeams',
    children: teamSpaceSubMenus,
    }

    return [...baseMenuItems, myTeamMenuItem, teamSpaceMenuGroup]
  }

  return [...baseMenuItems, myTeamMenuItem]
})

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    message.error('Failed to load my team space, ' + res.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})

const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>()
// 监听路由变化，更新高亮菜单项
router.afterEach((to) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}
</script>

<style scoped>
.floating-sider {
  position: fixed;
  top: 50%;
  left: 10px;
  transform: translateY(-50%);
  z-index: 999;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  width: 60px;
  height: auto;
}

.floating-sider.expanded {
  width: 280px;
}

/* 触发按钮区域 */
.trigger-area {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.floating-sider.expanded .trigger-area {
  left: 220px;
  opacity: 0;
  pointer-events: none;
}

.trigger-button {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(6px);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.trigger-button:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.trigger-button .anticon {
  font-size: 16px;
  color: #1890ff;
}

/* 侧边栏内容 */
.sider-content {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  width: 260px;
  min-height: 70vh;
  max-height: 85vh;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.4);
  padding: 24px 0;
  animation: slideIn 0.3s ease-out;
  overflow-y: auto;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-50%) translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) translateX(0);
  }
}

/* 侧边栏头部 */
.sider-header {
  display: flex;
  align-items: center;
  padding: 0 24px 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.sider-header .logo {
  width: 36px;
  height: 36px;
  margin-right: 12px;
}

.sider-header .title {
  font-size: 18px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}

/* 菜单样式 */
.floating-menu {
  background: transparent !important;
  border: none !important;
  padding: 0 16px;
}

:deep(.floating-menu .ant-menu-item) {
  margin: 6px 0 !important;
  padding: 0 20px !important;
  border-radius: 12px !important;
  height: 44px !important;
  line-height: 44px !important;
  transition: all 0.2s ease !important;
  font-size: 15px !important;
}

:deep(.floating-menu .ant-menu-item:hover) {
  background: rgba(24, 144, 255, 0.12) !important;
  color: #1890ff !important;
}

:deep(.floating-menu .ant-menu-item-selected) {
  background: rgba(24, 144, 255, 0.18) !important;
  color: #1890ff !important;
  font-weight: 600 !important;
}

:deep(.floating-menu .ant-menu-item)::after {
  display: none !important;
}

:deep(.floating-menu .ant-menu-item-group-title) {
  padding: 12px 20px 8px 20px !important;
  font-size: 13px !important;
  color: rgba(0, 0, 0, 0.45) !important;
  font-weight: 600 !important;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

:deep(.floating-menu .ant-menu-item-group-list) {
  margin: 0 !important;
}

/* 图标样式 */
:deep(.floating-menu .anticon) {
  margin-right: 12px;
  font-size: 18px;
}
</style>
