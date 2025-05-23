<template>
  <div id="pictureDetailPage">
    <!-- 背景模糊容器 -->
    <div
      class="picture-detail-wrapper"
      :style="{
        backgroundImage: picture.url ? `url(${picture.url})` : 'none'
      }"
    >
      <div class="content-overlay">
        <!--    列的横向和竖向间距都是16-->
        <a-row :gutter="[16, 16]">
          <!-- 图片展示区 -->
          <a-col :sm="24" :md="16" :xl="18">
            <a-card :bordered="false">
              <a-image
                style="max-height: 600px; object-fit: contain"
                :src="picture.url"
              />
            </a-card>
          </a-col>
          <!-- 图片信息区 -->
          <a-col :sm="24" :md="8" :xl="6">
            <a-card :bordered="false">
              <a-descriptions :column="1">
                <a-descriptions-item>
                  <a-space>
                    <a-avatar :size="24" :src="picture.user?.userAvatar" />
                    <div>{{ picture.user?.userName }}</div>
                  </a-space>
                </a-descriptions-item>
                <a-descriptions-item label="Name">
                  {{ picture.name ?? 'unnamed' }}
                </a-descriptions-item>
                <a-descriptions-item label="Introduction">
                  {{ picture.introduction ?? '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="Category">
                  {{ picture.category ?? 'Default' }}
                </a-descriptions-item>
                <a-descriptions-item label="Tags">
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="Format">
                  {{ picture.picFormat ?? '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="Width">
                  {{ picture.picWidth ?? '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="Height">
                  {{ picture.picHeight ?? '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="Ratio">
                  {{ picture.picScale ?? '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="Size">
                  {{ formatSize(picture.picSize) }}
                </a-descriptions-item>
                <!-- <a-descriptions-item label="Color tone">
                  <a-space>
                    {{ picture.picColor ?? '-' }}
                    <div :style="{
                      width: '24px',
                      height: '18px',
                      backgroundColor: picture.picColor ? toHexColor(picture.picColor) : 'transparent'
                    }"
                    >
                  </div>
                  </a-space>
                </a-descriptions-item> -->
              </a-descriptions>
              <!--          图片操作-->
              <a-space wrap>
                <a-button :icon="h(ShareAltOutlined)"  type="primary" ghost @click="doShare">
                  Share
                </a-button>
                <a-button :icon="h(EditOutlined)" v-if="canEdit" type="default" @click="doEdit">
                  Edit
                </a-button>
                <a-button :icon="h(DeleteOutlined)" v-if="canDelete" danger @click="doDelete">
                  Delete
                </a-button>
                <a-button
                  v-if="isAdmin"
                  type="primary"
                  @click="handleReview(picture, PIC_REVIEW_STATUS_ENUM.PASS)"
                >
                  Pass
                </a-button>
                <a-button
                  v-if="isAdmin"
                  danger
                  @click="handleReview(picture, PIC_REVIEW_STATUS_ENUM.REJECT)"
                >
                  Reject
                </a-button>
                <a-button type="primary" @click="doDownload">
                  Download Original
                  <template #icon>
                    <DownloadOutlined />
                  </template>
                </a-button>
              </a-space>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </div>
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { DeleteOutlined, DownloadOutlined, EditOutlined, ShareAltOutlined } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref, h } from 'vue'
import {
  deletePictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
  doPictureReviewUsingPost
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { downloadImage, formatSize, toHexColor } from '../utils'
import router from '@/router'
import { PIC_REVIEW_STATUS_ENUM } from '@/constants/picture.ts'
import ShareModal from '@/components/ShareModal.vue'
import { SPACE_PERMISSION_ENUM } from '@/constants/space.ts'

const loginUserStore = useLoginUserStore()

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (picture.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canEdit = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDelete = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)


// // 是否具有编辑权限
// const canEdit = computed(() => {
//   const loginUser = loginUserStore.loginUser
//   // 未登录不可编辑
//   if (!loginUser.id) {
//     return false
//   }
//   // 仅本人或管理员可编辑
//   const user = picture.value.user || {}
//   return loginUser.id === user.id || loginUser.userRole === 'admin'
// })

// 是否是管理员
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 处理审核
const handleReview = async (pic: API.Picture, reviewStatus: number) => {
  const reviewMessage = reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS
    ? 'Admin operation: Passed'
    : 'Admin operation: Rejected'

  const res = await doPictureReviewUsingPost({
    id: pic.id,
    reviewStatus,
    reviewMessage
  })

  if (res.data.code === 0) {
    message.success('Review operation successful')
    // 刷新图片信息
    // fetchPictureDetail()
  } else {
    message.error('Review operation failed, ' + res.data.message)
  }
}

// 处理下载
const doDownload = () => {
  downloadImage(picture.value.originalUrl)
}

// 编辑
const doEdit = () => {
  // 跳转时携带spaceId
  router.push({
    path: `/add_picture`,
    query: {
      id: picture.value.id,
      spaceId: picture.value.spaceId
    }
  })
}

// 删除
const doDelete = async () => {
  const id = picture.value.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('Deleted successfully')
    // 删除成功后返回上一页
    router.go(-1)
  } else {
    message.error('Delete failed: ' + (res.data.message || ''))
  }
}

interface Props {
  id: string | number
}

const props = defineProps<Props>()

const picture = ref<API.PictureVO>({})

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: props.id
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('Failed to get picture information, ' + res.data.message)
    }
  } catch (e: any) {
    message.error('Failed to get picture information: ' + e.message)
  }
}

onMounted(() => {
  fetchPictureDetail()
})

// -----------分享操作-------------

const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>('')
// 分享函数
const doShare = (e) => {

  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.value.id}`
  // shareModalRef 是一个 Vue 的 ref 引用，用于获取子组件 ShareModal 的实例
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}



</script>

<style scoped>
#pictureDetailPage {
  margin-bottom: 16px;
}

/* 背景模糊容器 */
.picture-detail-wrapper {
  position: relative;
  min-height: 100vh;
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  background-attachment: local;
  border-radius: 24px;
  overflow: hidden;
  margin: 16px;
}

/* 在背景图上的遮罩层 - 超强模糊效果 */
.picture-detail-wrapper::before {
  content: '';
  position: absolute;
  top: -50px;
  left: -50px;
  right: -50px;
  bottom: -50px;
  background-image: inherit;
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  filter: blur(150px) saturate(1.5) brightness(1.1);
  z-index: -1;
}

/* 额外的模糊层，改blur参数调整模糊效果，background时透明度，为1的时候图片直接消失 */
.picture-detail-wrapper::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(50px);
  z-index: 0;
}

/* 内容覆盖层 */
.content-overlay {
  position: relative;
  z-index: 1;
  background: transparent;
  padding: 32px;
  min-height: 100vh;
  border-radius: 24px;
  display: flex;
  align-items: center;
}

/* 等高行列容器 */
:deep(.ant-row) {
  background: transparent;
  margin: 0 !important;
  height: 90vh;
  min-height: 600px;
  width: 100%;
}

:deep(.ant-col) {
  background: transparent;
  padding: 8px !important;
  height: 100%;
}

/* 卡片样式优化 - 强烈毛玻璃效果 */
:deep(.ant-card) {
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(30px) saturate(1.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 16px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.ant-card-body) {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  border-radius: 0;
  padding: 24px;
  margin: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 图片预览卡片特殊处理 - 居中显示图片 */
:deep(.ant-col:first-child .ant-card-body) {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
}

:deep(.ant-image) {
  display: block;
  text-align: center;
  max-height: 95%;
  width: 100%;
}

:deep(.ant-image img) {
  max-height: 85vh;
  object-fit: contain;
  width: auto;
  max-width: 100%;
}

/* 信息卡片内容布局 */
:deep(.ant-col:last-child .ant-card-body) {
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(18px);
}

/* 操作按钮区域 */
:deep(.ant-space) {
  margin-top: auto;
  padding-top: 16px;
}

/* 按钮样式增强 */
:deep(.ant-btn) {
  backdrop-filter: blur(10px);
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

:deep(.ant-btn-primary) {
  background: rgba(24, 144, 255, 0.95);
  border: 1px solid rgba(24, 144, 255, 0.8);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

:deep(.ant-btn-primary:hover) {
  background: rgba(24, 144, 255, 1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

:deep(.ant-btn-default) {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(217, 217, 217, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  color: rgba(0, 0, 0, 0.85);
}

:deep(.ant-btn-default:hover) {
  background: rgba(255, 255, 255, 1);
  transform: translateY(-1px);
}

:deep(.ant-btn-dangerous) {
  background: rgba(255, 77, 79, 0.95);
  border: 1px solid rgba(255, 77, 79, 0.8);
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
  color: #fff;
}

:deep(.ant-btn-dangerous:hover) {
  background: rgba(255, 77, 79, 1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
}

/* 描述列表样式增强 */
:deep(.ant-descriptions) {
  background: transparent;
}

:deep(.ant-descriptions-item) {
  background: transparent;
}

:deep(.ant-descriptions-item-container) {
  background: transparent;
}

:deep(.ant-descriptions-item-label) {
  color: rgba(0, 0, 0, 0.8);
  font-weight: 500;
}

:deep(.ant-descriptions-item-content) {
  color: rgba(0, 0, 0, 0.9);
}

/* 标签样式增强 */
:deep(.ant-tag) {
  background: rgba(24, 144, 255, 0.1);
  border: 1px solid rgba(24, 144, 255, 0.3);
  backdrop-filter: blur(5px);
}

/* 头像样式 */
:deep(.ant-avatar) {
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
