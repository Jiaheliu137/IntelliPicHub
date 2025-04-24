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
            <template v-if="showOp" #actions>
              <a-space @click="(e) => doShare(picture, e)">
                <ShareAltOutlined />
                <!--                Share-->
              </a-space>
              <a-space @click="(e) => doSearch(picture, e)">
                <SearchOutlined />
                <!--                Search-->
              </a-space>
              <a-space @click="e => doEdit(picture,e)">
                <EditOutlined />
                <!--                Edit-->
              </a-space>
              <a-space @click="e => doDelete(picture,e)">
                <DeleteOutlined />
                <!--                Delete-->
              </a-space>
            </template>
            <div class="picture-overlay">
              <h3 class="picture-title">{{ picture.name }}</h3>
              <div class="picture-tags">
                <a-tag color="green">
                  {{ picture.category ?? 'Other' }}
                </a-tag>
                <a-tag v-for="tag in picture.tags" :key="tag">
                  {{ tag }}
                </a-tag>
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
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false
})

const router = useRouter()

/**
 * 跳转至图片详情页
 * @param picture 图片信息
 */
const doClickPicture = (picture: API.PictureVO) => {
  window.open(`/picture/${picture.id}`, '_blank')
}

const doSearch = (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  // 打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

// 编辑
// e 是事件对象（Event Object）的简写它包含了事件的所有信息，比如：e.target - 触发事件的元素e.stopPropagation() - 阻止事件冒泡的方法e.preventDefault() - 阻止默认行为的
const doEdit = (picture, e) => {
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
const doDelete = async (picture, e) => {
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
const doShare = (picture, e) => {
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

.picture-card :deep(.ant-card-body) {
  padding: 0;
  position: relative;
  height: 0;
}

.picture-card :deep(.ant-card-actions) {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background-color: rgba(255, 255, 255, var(--overlay-opacity));
  transform: translateY(-100%);
  transition: transform 0.3s ease;
  z-index: 2;
}

.picture-card:hover :deep(.ant-card-actions) {
  transform: translateY(0);
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
  background: rgba(255, 255, 255, var(--overlay-opacity));
  transform: translateY(100%);
  transition: transform 0.3s ease;
  z-index: 1;
}

.picture-card:hover .picture-overlay {
  transform: translateY(0);
}

.picture-title {
  margin: 0 0 8px 0;
  color: rgba(0, 0, 0, var(--text-opacity));
  font-size: 16px;
  font-weight: bold;
}

.picture-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.picture-tags :deep(.ant-tag) {
  background-color: rgba(255, 255, 255, var(--overlay-opacity));
  color: rgba(0, 0, 0, var(--text-opacity));
  border: none;
}

.picture-tags :deep(.ant-tag-green) {
  background-color: rgba(255, 255, 255, var(--overlay-opacity));
  color: rgba(82, 196, 26, var(--text-opacity));
  border: 1px solid rgba(82, 196, 26, var(--text-opacity));
}

</style>
