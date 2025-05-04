<template>
  <div id="spaceDetailPage">
    <!--    空间信息-->
    <a-flex justify="space-between" align="bottom">
      <a-flex align="center">
        <h2>{{ space.spaceName }} ({{ SPACE_TYPE_MAP[space.spaceType] }})</h2>
        <a-space>
          <a-tag v-if="space.spaceLevel === 0" color="blue" class="space-level-tag">
            <star-outlined />
            {{ SPACE_LEVEL_MAP[0] }} Space
          </a-tag>
          <a-tag v-else-if="space.spaceLevel === 1" color="green" class="space-level-tag">
            <trophy-outlined />
            {{ SPACE_LEVEL_MAP[1] }} Space
          </a-tag>
          <a-tag v-else-if="space.spaceLevel === 2" color="gold" class="space-level-tag">
            <crown-outlined />
            {{ SPACE_LEVEL_MAP[2] }} Space
          </a-tag>
          <a-tag v-else color="blue" class="space-level-tag">
            <star-outlined />
            {{ SPACE_LEVEL_MAP[0] }} Space
          </a-tag>
        </a-space>
      </a-flex>
      <a-space size="middle">
        <a-button
          v-if="canUploadPicture"
          type="primary"
          @click="showUploadModal"
        >
          + Add Picture
        </a-button>
        <a-button
          v-if="canManageSpaceUser &&SPACE_TYPE_ENUM.TEAM==space.spaceType"
          type="primary"
          ghost
          :icon="h(TeamOutlined)"
          :href="`/spaceUserManage/${id}`"
          target="_blank"
        >
          Member manage
        </a-button>

        <a-button
          v-if="canManageSpaceUser"
          type="primary"
          ghost :icon="h(BarChartOutlined)"
          :href="`/space_analyze?spaceId=${id}`" target="_blank"
        >
          Space Analyze
        </a-button>
        <a-button
          v-if="canEditPicture"
          :icon="h(EditOutlined)"
          @click="doBatchEdit"
        >
          Batch Edit
        </a-button>
        <a-tooltip
          :title="`Used Space ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
          placement="bottomLeft"
          :mouseEnterDelay="0"
          overlayClassName="space-usage-tooltip"
        >
          <a-progress
            type="circle"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
            :size="42"
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"></div>
    <!--搜索表单-->
    <PictureSearchForm :onSearch="onSearch" />

    <!-- 按颜色搜索 -->
    <a-form-item label="Search Color" style="margin-top: 16px">
      <color-picker format="hex" @pureColorChange="onColorChange" />
    </a-form-item>

    <div style="margin-bottom: 16px"></div>


    <!-- 图片列表 -->
    <PictureList
      :dataList="dataList"
      :loading="loading"
      :showOp="true"
      :onReload="fetchData"
      :canEdit="canEditPicture"
      :canDelete="canDeletePicture"
    />
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      :show-size-changer="true"
      :pageSizeOptions="['12', '24', '36', '48']"
      @change="onPageChange"
    />

    <!-- 使用图片上传弹窗组件 -->
    <PicturePopUpUploadModal
      v-model:visible="uploadModalVisible"
      :spaceId="id"
      :categoryOptions="categoryOptions"
      :tagOptions="tagOptions"
      @success="fetchData"
    />
    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :spaceId="id"
      :editPictureList="dataList"
      :categoryOptions="categoryOptions"
      :tagOptions="tagOptions"
      @success="onBatchEditPictureSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import {
  BarChartOutlined,
  CrownOutlined, EditOutlined,
  StarOutlined, TeamOutlined,
  TrophyOutlined
} from '@ant-design/icons-vue'
import { onMounted, ref, h, watch, computed } from 'vue'
import {
  getSpaceVoByIdUsingGet

} from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
  searchPictureByColorUsingPost
} from '@/api/pictureController.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import { SPACE_LEVEL_MAP, SPACE_PERMISSION_ENUM, SPACE_TYPE_ENUM, SPACE_TYPE_MAP } from '@/constants/space.ts'
import PicturePopUpUploadModal from '@/components/PicturePopUpUploadModal.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'


// 数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 分类，标签
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

// 分类和标签选项
const categoryOptions = ref<{ value: string, label: string }[]>([])
const tagOptions = ref<{ value: string, label: string }[]>([])

interface Props {
  id: string | number
}

const props = defineProps<{
  id: string | number
}>()

const space = ref<API.SpaceVO>({})

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)


// ------------获取空间详情------------
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('Failed to get space information，' + res.data.message)
    }
  } catch (e: any) {
    message.error('Failed to get space information：' + e.message)
  }
}

onMounted(() => {
  fetchSpaceDetail()
})

// ------------获取图片列表------------
// 搜索条件
// ref 包装的是一个带有 .value 的引用对象可以完全替换 .value 的内容，Vue 会保持响应性
// reactive 直接代理对象本身必须保持对象的引用不变，只能修改其属性如果整体替换会丢失响应性
// 为了后面整体替换这里用ref
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 24,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
    tags: [] as String[]
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('Failed to get data，' + res.data.message)
  }
  loading.value = false
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

const doSearch = () => {
  // 重置搜索条件
  searchParams.value.current = 1
  fetchData()
}


// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    categoryList.value = res.data.data.categoryList ?? []
    tagList.value = res.data.data.tagList ?? []
    // 初始化标签选择状态
    selectedTagList.value = new Array(tagList.value.length).fill(false)

    // 转换为选项格式
    categoryOptions.value = categoryList.value.map(category => ({
      value: category,
      label: category
    }))

    tagOptions.value = tagList.value.map(tag => ({
      value: tag,
      label: tag
    }))
  } else {
    message.error('Fail to load category and tags，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})



watch(
  () => props.id,
  (newSpaceId) => {
    fetchSpaceDetail()
    fetchData()
  },
)




// 图片上传相关
const uploadModalVisible = ref(false)

// 显示上传弹窗
const showUploadModal = () => {
  uploadModalVisible.value = true
}

const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  console.log('new', newSearchParams)
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1
  }
  console.log('old', searchParams)
  fetchData()
}

// ------------按图片搜索--------------
const onColorChange = async (color: string) => {
  loading.value = true
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id
  })
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data ?? []
    dataList.value = data
    total.value = data.length
  } else {
    message.error('Fail to get data，' + res.data.message)
  }
  loading.value = false

}

// ------------批量编辑--------------
const batchEditPictureModalRef = ref()

// 批量编辑成功
const onBatchEditPictureSuccess = () => {
  fetchData()
}

const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

</script>

<style scoped>
#spaceDetailPage {
  margin-bottom: 16px;
}

h2 {
  margin-right: 10px;
  margin-bottom: 0;
}

.space-level-tag {
  margin-left: 10px;
}

:global(.space-usage-tooltip) {
  position: fixed;
  z-index: 1000;
}
</style>
