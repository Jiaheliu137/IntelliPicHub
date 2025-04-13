<template>
  <div id="pictureManagePage">
    <a-flex justify="space-between">
      <h2>Picture Manage</h2>
      <a-space>
        <a-button type="primary" href="/add_picture" target="_blank">+ Create picture</a-button>
        <a-button type="primary" href="/add_picture/batch" target="_blank" ghost>+ Create pictures in batches</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"></div>

    <!--    搜索表单-->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="Keywords">
        <a-input
          v-model:value="searchParams.searchText"
          placeholder="search in intro,name"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="Category">
        <a-input v-model:value="searchParams.category" placeholder="Input category" allow-clear />
      </a-form-item>

      <a-form-item label="Tags">
        <a-select
          v-model:value="searchParams.tags"
          mode="tags"
          placeholder="Input tags"
          style="min-width: 180px"
          allow-clear
        >


        </a-select>
      </a-form-item>
      <a-form-item label="ReviewStatus" name="reviewStatus">
        <a-select
          v-model:value="searchParams.reviewStatus"
          :options="PIC_REVIEW_STATUS_OPTIONS"
          placeholder="Please select reviewStatus"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>


      <a-form-item>
        <a-button type="primary" html-type="submit">Search</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
    <!--    表格-->
    <!--      如果不想让表格被挤压可以在s-table里加这一句   :scroll="{ x: 'max-content' }"-->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"

    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="120" />
        </template>


        <template v-if="column.dataIndex === 'tags'">
          <a-space wrap>
            <a-tag v-for="tag in JSON.parse(record.tags || '[]')" :key="tag" color="blue">{{ tag }}</a-tag>
          </a-space>

        </template>

        <template v-if="column.dataIndex === 'picInfo'">
          <div>Format：{{ record.picFormat }}</div>
          <div>Width：{{ record.picWidth }}</div>
          <div>Heigth：{{ record.picHeight }}</div>
          <div>Ratio：{{ record.picScale }}</div>
          <div>Size：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <!-- 审核信息 -->
        <template v-if="column.dataIndex === 'reviewMessage'">
          <div>ReviewStatus：{{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>ReviewMessage：{{ record.reviewMessage }}</div>
          <div>Reviewer：{{ record.reviewerId }}</div>
          <div v-if="record.reviewTime">
            ReviewTime：{{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}
          </div>
        </template>


        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
              type="link"
              @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
            >
              Pass
            </a-button>
            <a-button
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
              type="link"
              danger
              @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
            >
              Reject
            </a-button>
            <a-button type="link" :href="`/add_picture?id=${record.id}`" target="_blank">Edit</a-button>
            <a-button danger @click="doDelete(record.id)">Delete</a-button>
          </a-space>

        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  deletePictureUsingPost, doPictureReviewUsingPost,
  listPictureByPageUsingPost,
  listPictureVoByPageUsingPost
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PIC_REVIEW_STATUS_ENUM, PIC_REVIEW_STATUS_MAP, PIC_REVIEW_STATUS_OPTIONS } from '../../constants/picture.ts'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80
  },
  {
    title: 'Picture',
    dataIndex: 'url'
  },
  {
    title: 'Name',
    dataIndex: 'name'
  },
  {
    title: 'Introduction',
    dataIndex: 'introduction',
    ellipsis: true
  },
  {
    title: 'Category',
    dataIndex: 'category'
  },
  {
    title: 'Tags',
    dataIndex: 'tags'
  },
  {
    title: 'PicInfo',
    dataIndex: 'picInfo'
  },
  {
    title: 'UserId',
    dataIndex: 'userId',
    width: 80
  },
  {
    title: 'ReviewMessage',
    dataIndex: 'reviewMessage'
  },
  {
    title: 'CreateTime',
    dataIndex: 'createTime'
  },
  {
    title: 'EditTime',
    dataIndex: 'editTime'
  },
  {
    title: 'Action',
    key: 'action'
  }
]


// 定义数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `Total ${total}`
  }
})

// 表格变化后重新获取数据
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 获取数据
const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    // 将一个数组或对象展开成独立的元素或属性
    ...searchParams
  })
  if (res.data.code === 0 && res.data.data) {
    // 后端代码返回的是BaseResponse<Page<PictureVO>>类型，取值要套要套多层，要注意
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('Failed to get data,' + res.data.message)
  }
}

// 页面加载时执行一次函数请求一次数据
onMounted(() => {
  fetchData()
})

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除用户
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('Delete success')
    //刷新数据
    fetchData()
  } else {
    message.error('Delete failed,' + res.data.message)
  }
}

// 审核图片
const handleReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage = reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? 'Administrator operation passed' : 'Administrator operation rejected'

  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage
  })
  if (res.data.code === 0) {
    message.success('Review operation successed.')
    // 重新获取列表
    fetchData()
  } else {
    message.error('Review operation failed，' + res.data.message)
  }
}

</script>


