<template>
  <div id="userManagePage">
    <!--    搜索表单-->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="Account">
        <a-input
          v-model:value="searchParams.userAccount"
          placegolder="Input user account"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="Name">
        <a-input v-model:value="searchParams.userName" placegolder="Input user name" allow-clear />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit">Search</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
    <!--    表格-->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="20" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">Admin</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">User</a-tag>
          </div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'updateTime'">
          {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button danger @click="doDelete(record.id)">Delete</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUserUsingPost, listUserVoByPageUsingPost } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: 'Account',
    dataIndex: 'userAccount',
  },
  {
    title: 'Name',
    dataIndex: 'userName',
  },
  {
    title: 'Avatar',
    dataIndex: 'userAvatar',
  },
  {
    title: 'introduction',
    dataIndex: 'userProfile',
  },
  {
    title: 'Role',
    dataIndex: 'userRole',
  },
  {
    title: 'CreateTime',
    dataIndex: 'createTime',
  },
  {
    title: 'UpdateTime',
    dataIndex: 'updateTime',
  },
  {
    title: 'Action',
    key: 'action',
  },
]

// 定义数据
const dataList = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `Total ${total}`,
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
  const res = await listUserVoByPageUsingPost({
    // 将一个数组或对象展开成独立的元素或属性
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    // 后端代码返回的是BaseResponse<Page<UserVO>>类型，取值要套要套多层，要注意
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
  const res = await deleteUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('Delete success')
    //刷新数据
    fetchData()
  } else {
    message.error('Delete failed,' + res.data.message)
  }
}
</script>
