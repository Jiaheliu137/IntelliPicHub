<template>
  <div class="spaceUserAnalyze">
    <a-card title="User upload">
      <v-chart :option="options" style="height: 320px; max-width: 100%;" :loading="loading" />
      <template #extra>
        <a-space>
          <a-segmented v-model:value="timeDimension" :options="timeDimensionOptions" />
          <a-input-search placeholder="Input userId" enter-button="Search user" @search="doSearch" />
        </a-space>
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">


import VChart from 'vue-echarts'
import 'echarts'
import { computed, ref, watchEffect } from 'vue'
import { getSpaceUserAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import { options } from 'axios'


interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false
})

// 时间维度选项
const timeDimension = ref<'day' | 'week' | 'mounth'>('day')
// 分段选择器
const timeDimensionOptions = [
  {
    label: 'day',
    value: 'day',
  },
  {
    label: 'week',
    value: 'week',
  },
  {
    label: 'month',
    value: 'month',
  },
]
// 用户id选项
const userId = ref<string>()

// 图表数据
const dataList = ref<API.SpaceUserAnalyzeResponse[]>([])
const loading = ref(true)

const doSearch = (value: string) => {
  userId.value = value
}


/**
 * 加载数据
 */
const fetchData = async () => {
  loading.value = true
  const res = await getSpaceUserAnalyzeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
    timeDimension: timeDimension.value,
    userId: userId.value
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data
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

const options = computed(() => {
  const periods = dataList.value.map((item) => item.period) // 时间区间
  const counts = dataList.value.map((item) => item.count) // 上传数量

  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: periods, name: 'Time range' },
    yAxis: { type: 'value', name: 'Upload count' },
    series: [
      {
        name: 'Upload count',
        type: 'line',
        data: counts,
        smooth: true, // 平滑折线
        emphasis: {
          focus: 'series'
        }
      }
    ]
  }
})


</script>


<style scoped>

</style>
