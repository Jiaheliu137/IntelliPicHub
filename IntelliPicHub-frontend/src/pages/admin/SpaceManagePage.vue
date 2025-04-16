<template>
  <div id="spaceManagePage">
    <a-flex justify="space-between">
      <h2>Space Manage</h2>
      <a-space>
        <a-button type="primary" href="/add_space" target="_blank">+ Create space</a-button>
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

import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PIC_REVIEW_STATUS_ENUM, PIC_REVIEW_STATUS_MAP, PIC_REVIEW_STATUS_OPTIONS } from '../../constants/space.ts'
import { listSpaceByPageUsingPost } from '@/api/spaceController.ts'
import { SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS } from '../../constants/space.ts'
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
const dataList = ref([])
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


</script>


