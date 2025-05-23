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
            <a-tag v-if="isCurrentUser(record)" color="blue">Me</a-tag>
          </a-space>
        </template>
        <template v-if="column.dataIndex === 'spaceRole'">
          <!-- 如果是当前用户，只显示角色文本，不允许修改 -->
          <span v-if="isCurrentUser(record)" style="color: #1890ff; font-weight: 500;">
            {{ SPACE_ROLE_OPTIONS.find(option => option.value === record.spaceRole)?.label || record.spaceRole }}
          </span>
          <!-- 如果不是当前用户，显示下拉框允许修改 -->
          <a-select
            v-else
            v-model:value="record.spaceRole"
            :options="filteredRoleOptions"
            @change="(value) => editSpaceRole(value, record)"
          />
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <!-- 如果不是当前用户，才显示删除按钮 -->
            <a-button
              v-if="!isCurrentUser(record)"
              type="link"
              danger
              @click="doDelete(record.id)"
            >
              Delete
            </a-button>
            <span v-else style="color: #999; font-size: 12px;">Cannot delete yourself</span>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'

import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'

import { SPACE_ROLE_OPTIONS } from '../../constants/space.ts'

import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

import {
  addSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  listSpaceUserUsingPost
} from '@/api/spaceUserController.ts'

// 获取当前登录用户信息
const loginUserStore = useLoginUserStore()

// 定义属性
interface Props {
  id: string
}

const props = defineProps<Props>()

// 数据
const dataList = ref<API.SpaceUserVO[]>([])

// 添加成员表单
const formData = reactive<API.SpaceUserAddRequest>({})

/**
 * @description 判断是否是当前登录用户
 * @param {API.SpaceUserVO} record - 用户记录
 * @returns {boolean} 是否是当前用户
 */
const isCurrentUser = (record: API.SpaceUserVO): boolean => {
  return record.user?.id === loginUserStore.loginUser.id
}

/**
 * @description 过滤角色选项，移除admin选项
 */
const filteredRoleOptions = SPACE_ROLE_OPTIONS.filter(option => option.value !== 'admin')

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
  // 检查是否尝试修改自己的角色
  if (isCurrentUser(record)) {
    message.error('Cannot modify your own role')
    return
  }

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

  // 查找要删除的用户记录
  const recordToDelete = dataList.value.find(item => item.id === id)
  if (recordToDelete && isCurrentUser(recordToDelete)) {
    message.error('Cannot delete yourself')
    return
  }

  // 显示确认对话框
  const confirmed = await new Promise((resolve) => {
    Modal.confirm({
      title: 'Confirm Delete',
      content: `Are you sure you want to remove user "${recordToDelete?.user?.userName}" from this space?`,
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


