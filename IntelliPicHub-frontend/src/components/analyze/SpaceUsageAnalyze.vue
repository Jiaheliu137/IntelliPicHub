<template>
  <div class="spaceUserAnalyze">
    <a-flex gap="middle">
      <a-card title="Storage space" style="width: 50%">
        <div style="height: 320px;text-align: center">
          <h3>{{ formatSize(data.usedSize) }}/
            {{ data.maxSize ? formatSize(data.maxSize) : 'Unlimited' }}</h3>
          <a-progress type="dashboard" :percent="data.sizeUsageRatio ?? 0" />
        </div>
      </a-card>
      <a-card title="Number of pictures " style="width: 50%">
        <div style="height: 320px;text-align: center">
          <h3>{{ data.usedCount }}/
            {{ data.maxCount ? data.maxCount : 'Unlimited' }}</h3>
          <a-progress type="dashboard" :percent="data.countUsageRatio ?? 0" />
        </div>
      </a-card>
    </a-flex>

  </div>
</template>

<script setup lang="ts">

import VChart from 'vue-echarts'
import 'echarts'
import { ref, watchEffect } from 'vue'
import { getSpaceCategoryAnalyzeUsingPost, getSpaceUsageAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import { formatSize } from '@/utils'


interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false
})

const data = ref<API.SpaceUsageAnalyzeResponse>({})

// 加载状态
const loading = ref(true)


/**
 * 加载数据
 */
const fetchData = async () => {
  loading.value = true
  const res = await getSpaceUsageAnalyzeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId
  })
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data
  } else {
    message.error('Fail to get data，' + res.data.message)
  }
  loading.value = false
}

/**
 * 监听变量，参数改变时出发数据的重新加载
 */
watchEffect(() => {
  fetchData()
})

</script>


<style scoped>

</style>
