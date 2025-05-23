<template>
  <div class="pictureList">
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="props.dataList"
      :loading="props.loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!--          单张图片-->
          <a-card
            hoverable
            class="picture-card"
            @click="doClickPicture(picture)"
            :body-style="{ padding: '0' }"
          >
            <template #cover>
              <img
                class="picture-image"
                :alt="picture.name"
                :src="picture.thumbnailUrl??picture.url"
              />
            </template>
            <div class="picture-overlay">
              <!-- 操作按钮移动到这里 -->
              <div v-if="showOp" class="picture-actions">
                <a-tooltip title="Share">
                  <a-space @click="(e) => doShare(picture, e)">
                    <ShareAltOutlined />
                  </a-space>
                </a-tooltip>
                <a-tooltip title="Search similar pictures">
                  <a-space @click="(e) => doSearch(picture, e)">
                    <SearchOutlined />
                  </a-space>
                </a-tooltip>
                <a-tooltip title="Edit" v-if="canEdit">
                  <a-space  @click="e => doEdit(picture,e)">
                    <EditOutlined />
                  </a-space>
                </a-tooltip>
                <a-tooltip title="Delete" v-if="canDelete">
                  <a-space  @click="e => doDelete(picture,e)">
                    <DeleteOutlined />
                  </a-space>
                </a-tooltip>
              </div>
            </div>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>


<script setup lang="ts">
import { useRouter } from 'vue-router'
import { EditOutlined, DeleteOutlined, SearchOutlined, ShareAltOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import ShareModal from '@/components/ShareModal.vue'
import { ref } from 'vue'

/**
 * 组件属性
 */
interface Props {
  /** 图片数据列表 */
  dataList?: API.PictureVO[]
  /** 加载状态 */
  loading?: boolean
  showOp?: boolean
  onReload?: () => void
  canEdit?: false,
  canDelete?: false,
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
})

const router = useRouter()

/**
 * 跳转至图片详情页
 * @param picture 图片信息
 */
const doClickPicture = (picture: API.PictureVO) => {
  // window.open(`/picture/${picture.id}`, '_blank')
  router.push(`/picture/${picture.id}`)
}

const doSearch = (picture: API.PictureVO, e: MouseEvent) => {
  // 阻止冒泡
  e.stopPropagation()
  // 打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

// 编辑
// e 是事件对象（Event Object）的简写它包含了事件的所有信息，比如：e.target - 触发事件的元素e.stopPropagation() - 阻止事件冒泡的方法e.preventDefault() - 阻止默认行为的
const doEdit = (picture: API.PictureVO, e: MouseEvent) => {
  // 阻止冒泡
  e.stopPropagation()
  // 跳转时携带spaceId
  router.push({
    path: `/add_picture`,
    query: {
      id: picture.id,
      spaceId: picture.spaceId
    }
  })
}

// 删除
const doDelete = async (picture: API.PictureVO, e: MouseEvent) => {
  // 阻止冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('Deleted successfully')
    // 调用父组件传入的刷新函数
    props.onReload?.()
  } else {
    message.error('Delete failed')
  }
}

// -----------分享操作-------------

const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>('')
// 分享函数
const doShare = (picture: API.PictureVO, e: MouseEvent) => {
  // 阻止冒泡
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  // shareModalRef 是一个 Vue 的 ref 引用，用于获取子组件 ShareModal 的实例
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

</script>


<style scoped>
/* 定义CSS变量 */
.pictureList {
  --overlay-opacity: 0.6;
  --text-opacity: 0.6;
}

.picture-card {
  width: 100%;
  height: 250px;
  overflow: hidden;
  border-radius: 8px;
}


.picture-image {
  width: 100%;
  height: 250px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.picture-card:hover .picture-image {
  transform: scale(1.05);
}

.picture-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transform: translateY(100%);
  transition: transform 0.3s ease;
  z-index: 1;
}

.picture-card:hover .picture-overlay {
  transform: translateY(0);
}





.picture-actions {
  display: flex;
  justify-content: space-around;
  align-items: center;
  gap: 8px;
}





</style>
