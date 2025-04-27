<template>
  <div id="spaceManagePage">
    <a-flex justify="space-between">
      <h2>Space user manage</h2>
      <a-space>
        <a-button type="primary" href="/add_space" target="_blank">+ Create space</a-button>
        <a-button type="primary" ghost href="/space_analyze?queryPublic=1" target="_blank">Analyze public pictures
        </a-button>
        <a-button type="primary" ghost href="/space_analyze?queryAll=1" target="_blank">Analyze all pictures</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"></div>

    <!--    添加成员表单-->
    <a-form layout="inline" :model="formData" @finish="handleSubmit">
      <a-form-item label="User id" name="userId">
        <a-input v-model:value="formData.userId" placeholder="Input userId" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">Add member</a-button>
      </a-form-item>
    </a-form>

    <div style="margin-bottom: 16px" />
    <!--    表格-->
    <!--      如果不想让表格被挤压可以在s-table里加这一句   :scroll="{ x: 'max-content' }"-->
    <a-table
      :columns="columns"
      :data-source="dataList"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userInfo'">
          <a-space>
            <a-avatar :src="record.user?.userAvatar" />
            {{ record.user?.userName }}
          </a-space>
        </template>
        <template v-if="column.dataIndex === 'spaceRole'">
          <a-select
            v-model:value="record.spaceRole"
            :options="SPACE_ROLE_OPTIONS"
            @change="(value) => editSpaceRole(value, record)"
          />
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" danger @click="doDelete(record.id)">Delete</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'

import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

import { SPACE_ROLE_OPTIONS } from '../../constants/space.ts'

import {
  addSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  listSpaceUserUsingPost
} from '@/api/spaceUserController.ts'

// 定义属性
interface Props {
  id: string
}

const props = defineProps<Props>()

// 数据
const dataList = ref<API.SpaceUserVO[]>([])

// 添加成员表单
const formData = reactive<API.SpaceUserAddRequest>({})

// 创建成员
const handleSubmit = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await addSpaceUserUsingPost({
    spaceId,
    ...formData,
  })
  if (res.data.code === 0) {
    message.success('Add success.')
    // 刷新数据
    fetchData()
  } else {
    message.error('Failed to add，' + res.data.message)
  }
}

// 表格列
const columns = [
  {
    title: 'User',
    dataIndex: 'userInfo'
  },
  {
    title: 'Role',
    dataIndex: 'spaceRole'
  },
  {
    title: 'Create time',
    dataIndex: 'createTime'
  },
  {
    title: 'Action',
    key: 'action'
  }
]





// 获取数据
const fetchData = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }

  const res = await listSpaceUserUsingPost({
    spaceId,
  })
  if (res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('Failed to get data，' + res.data.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

const editSpaceRole = async (value, record) => {
  const res = await editSpaceUserUsingPost({
    id: record.id,
    spaceRole: value
  })
  if (res.data.code === 0) {
    message.success('Edit success')
  } else {
    message.error('Failed to edit，' + res.data.message)
  }
}


// 删除空间
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('Delete success')
    // 刷新数据
    fetchData()
  } else {
    message.error('Failed to delete')
  }
}


</script>


