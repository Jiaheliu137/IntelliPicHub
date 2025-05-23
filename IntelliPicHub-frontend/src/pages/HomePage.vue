<template>
  <div id="homePage">
    <!-- 搜索框 -->
    <div class="search-bar">
      <a-input-search
        placeholder="Search from countless pictures"
        v-model:value="searchParams.searchText"
        enter-button="Search"
        size="large"
        @search="doSearch"
      />
    </div>

    <!-- 分类 + 标签 -->
    <a-tabs v-model:activeKey="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="All" />
      <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" />
    </a-tabs>
    <div class="tag-bar">
      <span style="margin-right: 8px">Tag：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTagList[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>


    <!-- 图片列表 -->
    <PictureList
      :dataList="dataList"
      :loading="loading"
      :showOp="true"
      :onReload="fetchData"
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

  </div>
  <!-- 添加分享模态框组件 -->
  <ShareModal ref="shareModalRef" :link="shareLink" title="分享图片" />
</template>


<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listPictureTagCategoryUsingGet, listPictureVoByPageUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import PictureList from '@/components/PictureList.vue'
import ShareModal from '@/components/ShareModal.vue'

// 设置组件名称以匹配路由配置，这样keep-alive才能正确缓存
defineOptions({
  name: 'Home'
})

// 数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 分享功能
const shareModalRef = ref()
const shareLink = ref<string>('')

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 24,
  sortField: 'createTime',
  sortOrder: 'descend',
  searchText: ''
})

// 分类，标签
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    ...searchParams,
    tags: [] as string[]
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })

  try {
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('Failed to get data，' + res.data.message)
  }
  } catch (error) {
    console.error('Failed to fetch data:', error)
    message.error('Failed to get data')
  } finally {
  loading.value = false
}
}

const doSearch = () => {
  // 重置页码但保留其他搜索条件
  searchParams.current = 1
  fetchData()
}

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  try {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    categoryList.value = res.data.data.categoryList ?? []
    tagList.value = res.data.data.tagList ?? []
    // 初始化标签选择状态
    selectedTagList.value = new Array(tagList.value.length).fill(false)
  } else {
    message.error('Fail to load category and tags，' + res.data.message)
    }
  } catch (error) {
    console.error('Failed to get tag/category options:', error)
    message.error('Failed to load categories and tags')
  }
}

// 组件挂载时初始化
onMounted(() => {
  getTagCategoryOptions()
  fetchData()
})
</script>


<style scoped>
#homePage {
  margin-bottom: 16px;
}

#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}

#homePage .tag-bar {
  margin-bottom: 16px;
}

</style>
