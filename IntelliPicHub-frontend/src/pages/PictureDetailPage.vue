<template>
  <div id="pictureDetailPage">
    <!--    列的横向和竖向间距都是16-->
    <a-row :gutter="[16, 16]">
      <!-- 图片展示区 -->
      <a-col :sm="24" :md="16" :xl="18">
        <a-card title="Preview picture">
          <a-image
            style="max-height: 600px; object-fit: contain"
            :src="picture.url"
          />
        </a-card>
      </a-col>
      <!-- 图片信息区 -->
      <a-col :sm="24" :md="8" :xl="6">
        <a-card title="Information">
          <a-descriptions :column="1">
            <a-descriptions-item label="Author">
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
            <a-descriptions-item label="Color tone">
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
            </a-descriptions-item>
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
</style>
