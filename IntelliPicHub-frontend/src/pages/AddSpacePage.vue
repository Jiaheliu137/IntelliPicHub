<template>
  <div id="addSpacePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? 'Edit' : 'Create' }} {{ SPACE_TYPE_MAP[spaceType] }}
    </h2>

    <!--    空间信息表单-->
    <a-form
      name="spaceForm"
      layout="vertical"
      :model="spaceForm"
      @finish="handleSubmit"
    >
      <a-form-item name="spaceName" label="Name">
        <a-input
          v-model:value="spaceForm.spaceName"
          placeholder="Input space name"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="SpaceLevel" name="spaceLevel">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          :options="SPACE_LEVEL_OPTIONS"
          placeholder="Select space level"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>


      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%">
          {{ route.query?.id ? 'Edit' : 'Create' }}
        </a-button>
      </a-form-item>
    </a-form>
    <!--    空间级别介绍-->
    <a-card title="Space Level">
      <a-typography-paragraph>
        * Currently only the standard version is supported. If you need to upgrade your space, please contact
        <a href="https://github.com/Jiaheliu137/IntelliPicHub" target="_blank">Administrator</a>。
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList" :key="spaceLevel.text">
        {{ spaceLevel.text }}: Size {{ formatSize(spaceLevel.maxSize) }}, Max Pictures: {{ spaceLevel.maxCount }}
      </a-typography-paragraph>
    </a-card>

  </div>
</template>


<script setup lang="ts">

import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  addSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  editSpaceUsingPost, updateSpaceUsingPost
} from '@/api/spaceController.ts'
import { useRoute } from 'vue-router'
import router from '@/router'
import { SPACE_LEVEL_ENUM, SPACE_LEVEL_OPTIONS, SPACE_TYPE_ENUM, SPACE_TYPE_MAP } from '@/constants/space.ts'
import { formatSize } from '../utils'


const spaceForm = reactive<API.SpaceAddRequest | API.SpaceEditRequest>({
  spaceName: 'Personal Space',
  spaceLevel: SPACE_LEVEL_ENUM.COMMON
})

const route = useRoute()
const oldSpace = ref<API.SpaceVO>()
const loading = ref(false)
const spaceLevelList = ref<API.SpaceLevel[]>([])
// 空间类别,默认为私有空间
// 空间类别
const spaceType = computed(() => {
  if (route.query?.type) {
    return Number(route.query.type)
  }
  return SPACE_TYPE_ENUM.PRIVATE
})


/**
 * 获取空间详情用于编辑
 */
const getOldSpace = async () => {
  const id = route.query?.id
  if (id) {
    loading.value = true
    try {
      // 确保 id 是字符串，不进行 Number 转换
      const res = await getSpaceVoByIdUsingGet({
        id: id
      })
      if (res.data.code === 0 && res.data.data) {
        const data = res.data.data
        oldSpace.value = data
        // 将获取的数据填充到表单中
        spaceForm.spaceName = data.spaceName
        spaceForm.spaceLevel = data.spaceLevel
      } else {
        message.error('Failed to get space info, ' + res.data.message)
      }
    } catch (_) {
      message.error('Failed to get space info')
    } finally {
      loading.value = false
    }
  }
}


/**
 * 获取空间级别列表
 */
const fetchSpaceLevelList = async () => {
  try {
    const res = await listSpaceLevelUsingGet()
    if (res.data.code === 0 && res.data.data) {
      spaceLevelList.value = res.data.data
    } else {
      message.error('Failed to load space level, ' + res.data.message)
    }
  } catch (_) {
    message.error('Failed to load space level')
  }
}

// 页面加载时执行
onMounted(() => {
  getOldSpace()
  fetchSpaceLevelList()
})

/**
 * 表单提交事件
 */
const handleSubmit = async () => {
  const spaceId = oldSpace.value?.id
  loading.value = true
  try {
    // 根据是否有ID判断是创建还是更新
    const id = route.query?.id
    let res

    if (spaceId) {

      res = await updateSpaceUsingPost({
        id: spaceId, // 不转换为Number
        ...spaceForm
      })
    } else {
      // 创建空间
      res = await addSpaceUsingPost({
        ...spaceForm,
        spaceType: spaceType.value
      })
    }

    // 操作成功
    if (res.data.code === 0 && res.data.data) {
      const spaceId = id || res.data.data
      message.success(id ? 'Edit successfully' : 'Create successfully')

      // 检查是否是从管理页面来的编辑操作
      const fromAdmin = route.query?.fromAdmin === 'true'
      if (fromAdmin) {
        // 如果是管理员编辑用户空间，则返回空间管理页面
        router.push('/admin/spaceManage')
      } else {
        // 普通用户操作则跳转到空间详情页
        router.push({
          path: `/space/${spaceId}`
        })
      }
    } else {
      message.error((id ? 'Edit' : 'Create') + ' failed, ' + res.data.message)
    }
  } catch (_) {
    message.error((route.query?.id ? 'Edit' : 'Create') + ' failed')
  } finally {
    loading.value = false
  }
}

</script>


<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}

</style>
