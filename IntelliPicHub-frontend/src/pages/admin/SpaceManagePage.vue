<template>
  <div id="spaceManagePage">
    <a-flex justify="space-between">
      <h2>Space Manage</h2>
      <a-space>
        <a-button type="primary" href="/add_space" target="_blank">+ Create space</a-button>
        <a-button type="primary" ghost href="/space_analyze?queryPublic=1" target="_blank">Analyze public pictures</a-button>
        <a-button type="primary" ghost href="/space_analyze?queryAll=1" target="_blank">Analyze all pictures</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"></div>

    <!--    搜索表单-->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="SpaceName">
        <a-input
          v-model:value="searchParams.spaceName"
          placeholder="Input space name"
          allow-clear
        />
      </a-form-item>

      <a-form-item label="SpaceLevel" name="spaceLevel">
        <a-select
          v-model:value="searchParams.spaceLevel"
          :options="SPACE_LEVEL_OPTIONS"
          placeholder="Select space level"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="SpaceType" name="spaceType">
        <a-select
          v-model:value="searchParams.spaceLevel"
          :options="SPACE_TYPE_OPTIONS"
          placeholder="Select space type"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>

      <a-form-item label="UserId">
        <a-input v-model:value="searchParams.userId" placeholder="Input user id" allow-clear />
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


        <template v-if="column.dataIndex === 'spaceLevel'">
          <div>{{ SPACE_LEVEL_MAP[record.spaceLevel] }}</div>
        </template>
<!--        空间类别-->
        <template v-if="column.dataIndex === 'spaceType'">
          <a-tag>{{ SPACE_TYPE_MAP[record.spaceType] }}</a-tag>
        </template>


        <template v-if="column.dataIndex === 'spaceUseInfo'">
          <div>Size：{{ formatSize(record.totalSize) }}/{{ formatSize(record.maxSize) }}</div>
          <div>Count：{{ record.totalCount }}/{{ record.maxCount }}</div>
        </template>


        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" :href="`/space_analyze?spaceId=${record.id}`" target="_blank">Analyze</a-button>
            <a-button type="link" :href="`/add_space?id=${record.id}&fromAdmin=true`" target="_blank">Edit</a-button>
            <a-button danger @click="doDelete(record.id)">Delete</a-button>
          </a-space>

        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'

import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import { listSpaceByPageUsingPost, deleteSpaceUsingPost } from '@/api/spaceController.ts'
import {
  SPACE_LEVEL_MAP,
  SPACE_LEVEL_OPTIONS,
  SPACE_TYPE_ENUM,
  SPACE_TYPE_MAP,
  SPACE_TYPE_OPTIONS
} from '../../constants/space.ts'
import { formatSize } from '../../utils'


const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80
  },
  {
    title: 'SpaceName',
    dataIndex: 'spaceName'
  },
  {
    title: 'SpaceType',
    dataIndex: 'spaceType'
  },
  {
    title: 'SpaceLevel',
    dataIndex: 'spaceLevel'
  },
  {
    title: 'UseInfo',
    dataIndex: 'spaceUseInfo'
  },
  {
    title: 'UserId',
    dataIndex: 'userId',
    width: 80
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


// 数据
const dataList = ref<API.Space[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`
  }
})

// 获取数据
const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({
    ...searchParams
  })
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 获取数据
const doSearch = () => {
  // 重置搜索条件
  searchParams.current = 1
  fetchData()
}

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 删除空间
const doDelete = async (id: number) => {
  if (!id) {
    return
  }

  // 查找要删除的空间记录
  const recordToDelete = dataList.value.find(item => item.id === id)

  // 显示确认对话框
  const confirmed = await new Promise((resolve) => {
    Modal.confirm({
      title: 'Confirm Delete',
      content: `Are you sure you want to delete space "${recordToDelete?.spaceName || 'this space'}"? This action cannot be undone.`,
      okText: 'Yes, Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk() {
        resolve(true)
      },
      onCancel() {
        resolve(false)
      },
    })
  })

  if (!confirmed) {
    return
  }

  try {
    const res = await deleteSpaceUsingPost({
      id
    })
    if (res.data.code === 0) {
      message.success('Delete successfully')
      // 重新加载数据
      fetchData()
    } else {
      message.error('Delete failed: ' + res.data.message)
    }
  } catch (error) {
    console.error('Delete space failed:', error)
    message.error('Delete space failed, please try again later')
  }
}

</script>


