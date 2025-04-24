<template>
  <div class="spaceSizeAnalyze">
    <a-card title="Size analyze">
      <v-chart :option="options" style="height: 320px; max-width: 100%;" :loading="loading" />
    </a-card>
  </div>
</template>

<script setup lang="ts">


import VChart from 'vue-echarts'
import 'echarts'
import { computed, ref, watchEffect } from 'vue'
import { getSpaceSizeAnalyzeUsingPost} from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'



interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false
})

// 图表数据
const dataList = ref<API.SpaceSizeAnalyzeResponse[]>([])
const loading = ref(true)

/**
 * 加载数据
 */
const fetchData = async () => {
  loading.value = true
  const res = await getSpaceSizeAnalyzeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId
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
  const pieData = dataList.value.map((item) => ({
    name: item.sizeRange,
    value: item.count,
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      top: 'bottom',
    },
    series: [
      {
        name: 'Image size',
        type: 'pie',
        // 饼图的半径 = Math.min(容器宽度, 容器高度) * 50%
        radius: '50%',
        data: pieData,
      },
    ],
  }
})




</script>


<style scoped>

</style>
