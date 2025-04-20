<template>
  <div class="pictureSearchForm">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <!-- 常用搜索条件始终显示 -->
      <div class="basic-search-row">
        <a-form-item label="Keywords" name="searchText">
          <a-input
            v-model:value="searchParams.searchText"
            placeholder="Input name or intro"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item name="category" label="Category">
          <a-auto-complete
            v-model:value="searchParams.category"
            style="width: 150px"
            placeholder="Input categories"
            :options="categoryOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item name="tags" label="Tag">
          <a-select
            v-model:value="searchParams.tags"
            mode="tags"
            placeholder="Input tags"
            style="width: 180px"
            :options="tagOptions"
            allow-clear
          >
          </a-select>
        </a-form-item>
        <a-form-item class="btn-form-item">
          <a-button type="primary" html-type="submit" style="margin-right: 8px">Search</a-button>
          <a-button html-type="reset" @click="doClear" style="margin-right: 8px">Reset</a-button>
          <a-button type="link" @click="toggleAdvanced" style="padding: 4px 0; min-width: 24px">
            <DownOutlined :rotate="showAdvanced ? 180 : 0" />
          </a-button>
        </a-form-item>
      </div>

      <!-- 高级搜索条件 -->
      <a-collapse
        v-model:activeKey="activeCollapseKey"
        :bordered="false"
        expand-icon-position="end"
        class="advanced-search-collapse"
        ghost
      >
        <a-collapse-panel key="1" :show-arrow="false" class="no-padding-panel">
          <template #header></template>
          <div class="advanced-search-row">
            <a-form-item label="Date" name="">
              <a-range-picker
                style="width: 400px"
                show-time
                v-model:value="dateRange"
                :placeholder="['Edit start time', 'Edit end time']"
                format="YYYY/MM/DD HH:mm:ss"
                :presets="rangePresets"
                @change="onRangeChange"
              />
            </a-form-item>
            <a-form-item label="Name" name="name">
              <a-input v-model:value="searchParams.name" placeholder="Input name" allow-clear />
            </a-form-item>
            <a-form-item label="Introduction" name="introduction">
              <a-input v-model:value="searchParams.introduction" placeholder="Input introduction" allow-clear />
            </a-form-item>

            <!-- 宽高和格式一行显示 -->
            <div class="dimensions-format-row">
              <a-form-item label="Width" class="dimension-item">
                <div class="dimension-container">
                  <a-input-number
                    v-model:value="widthInputRange[0]"
                    placeholder="Min"
                    style="width: 70px"
                    :min="0"
                    @change="onWidthInputChange"
                  />
                  <span class="range-separator">-</span>
                  <a-input-number
                    v-model:value="widthInputRange[1]"
                    placeholder="Max"
                    style="width: 70px"
                    :min="0"
                    @change="onWidthInputChange"
                  />
                  <a-slider
                    v-model:value="widthRange"
                    range
                    :min="enableSliderMin ? minSliderWidth : undefined"
                    :max="maxSliderWidth"
                    style="width: 150px; margin-left: 8px"
                    @change="onWidthRangeChange"
                  />
                </div>
              </a-form-item>

              <a-form-item label="Height" class="dimension-item">
                <div class="dimension-container">
                  <a-input-number
                    v-model:value="heightInputRange[0]"
                    placeholder="Min"
                    style="width: 70px"
                    :min="0"
                    @change="onHeightInputChange"
                  />
                  <span class="range-separator">-</span>
                  <a-input-number
                    v-model:value="heightInputRange[1]"
                    placeholder="Max"
                    style="width: 70px"
                    :min="0"
                    @change="onHeightInputChange"
                  />
                  <a-slider
                    v-model:value="heightRange"
                    range
                    :min="enableSliderMin ? minSliderHeight : undefined"
                    :max="maxSliderHeight"
                    style="width: 150px; margin-left: 8px"
                    @change="onHeightRangeChange"
                  />
                </div>
              </a-form-item>

              <a-form-item label="Format" name="picFormat" class="format-item">
                <a-input
                  v-model:value="searchParams.picFormat"
                  placeholder="Input format"
                  allow-clear
                  style="width: 120px"
                />
              </a-form-item>
            </div>
          </div>
        </a-collapse-panel>
      </a-collapse>
    </a-form>


  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { listPictureTagCategoryUsingGet, searchPictureByColorUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { DownOutlined } from '@ant-design/icons-vue'


interface Props {
  onSearch?: (searchParams: Record<string, unknown>) => void
}

interface TagOption {
  value: string
  label: string
}

const props = defineProps<Props>()
const dateRange = ref<[]>([])
const categoryOptions = ref<TagOption[]>([])
const tagOptions = ref<TagOption[]>([])
// 搜索条件
const searchParams = reactive<Record<string, unknown>>({})
// 控制高级搜索的显示
const showAdvanced = ref(false)
// 控制折叠面板
const activeCollapseKey = computed(() => (showAdvanced.value ? ['1'] : []))

// 滑块控制的范围
const minSliderWidth = 360
const maxSliderWidth = 3840
const minSliderHeight = 360
const maxSliderHeight = 3840
// 是否启用滑块最小值限制
const enableSliderMin = ref(true)
// 宽度范围
const widthRange = ref<[number | null, number | null]>([null, null])
const widthInputRange = ref<[number | null, number | null]>([null, null])
// 高度范围
const heightRange = ref<[number | null, number | null]>([null, null])
const heightInputRange = ref<[number | null, number | null]>([null, null])

// 切换高级搜索显示状态
const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value
}

// 时间范围预设
const rangePresets = ref([
  { label: 'Past 7 days', value: [dayjs().add(-7, 'd'), dayjs()] },
  { label: 'Past 14 days', value: [dayjs().add(-14, 'd'), dayjs()] },
  { label: 'Past 30 days', value: [dayjs().add(-30, 'd'), dayjs()] },
  { label: 'Past 90 days', value: [dayjs().add(-90, 'd'), dayjs()] },
])




/**
 * 日期范围更改时触发
 * @param dates
 * @param dateStrings
 */
const onRangeChange = (dates: any[], dateStrings: string[]) => {
  if (dates.length >= 2) {
    searchParams.startEditTime = dates[0].toDate()
    searchParams.endEditTime = dates[1].toDate()
  } else {
    searchParams.startEditTime = undefined
    searchParams.endEditTime = undefined
  }
}

/**
 * 宽度输入框变化时触发
 */
const onWidthInputChange = () => {
  searchParams.minPicWidth = widthInputRange.value[0]
  searchParams.maxPicWidth = widthInputRange.value[1]
  // 如果输入值在滑块范围内，同步到滑块
  if (widthInputRange.value[0] !== null &&
      widthInputRange.value[1] !== null &&
      widthInputRange.value[0] >= minSliderWidth &&
      widthInputRange.value[1] <= maxSliderWidth) {
    widthRange.value = [...widthInputRange.value]
  }
  delete searchParams.picWidth
}

/**
 * 高度输入框变化时触发
 */
const onHeightInputChange = () => {
  searchParams.minPicHeight = heightInputRange.value[0]
  searchParams.maxPicHeight = heightInputRange.value[1]
  // 如果输入值在滑块范围内，同步到滑块
  if (heightInputRange.value[0] !== null &&
      heightInputRange.value[1] !== null &&
      heightInputRange.value[0] >= minSliderHeight &&
      heightInputRange.value[1] <= maxSliderHeight) {
    heightRange.value = [...heightInputRange.value]
  }
  delete searchParams.picHeight
}

/**
 * 宽度滑块变化时触发
 */
const onWidthRangeChange = () => {
  if (!enableSliderMin.value) {
    enableSliderMin.value = true
  }
  // 同步到输入框
  widthInputRange.value = [...widthRange.value]
  searchParams.minPicWidth = widthRange.value[0]
  searchParams.maxPicWidth = widthRange.value[1]
  delete searchParams.picWidth
}

/**
 * 高度滑块变化时触发
 */
const onHeightRangeChange = () => {
  if (!enableSliderMin.value) {
    enableSliderMin.value = true
  }
  // 同步到输入框
  heightInputRange.value = [...heightRange.value]
  searchParams.minPicHeight = heightRange.value[0]
  searchParams.maxPicHeight = heightRange.value[1]
  delete searchParams.picHeight
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  props.onSearch?.(searchParams)
}

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data
      }
    })
  } else {
    message.error('Failed to load options，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

// 取消所有对象的值
const doClear = () => {
  Object.keys(searchParams).forEach(key => {
    searchParams[key] = undefined
  })
  // 临时禁用滑块最小值限制
  enableSliderMin.value = false
  // 清空宽高范围相关的所有参数
  widthRange.value = [null, null]
  heightRange.value = [null, null]
  widthInputRange.value = [null, null]
  heightInputRange.value = [null, null]
  searchParams.minPicWidth = undefined
  searchParams.maxPicWidth = undefined
  searchParams.minPicHeight = undefined
  searchParams.maxPicHeight = undefined
  // 日期的值必须是数组，因此单独清空
  dateRange.value = []
  // 清空后重新搜索
  props.onSearch?.(searchParams)
}
</script>

<style scoped>
.pictureSearchForm {
  width: 100%;
}

.basic-search-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 0;
}

.btn-form-item {
  margin-left: auto;
  margin-right: 0;
  display: flex;
  align-items: center;
}

.advanced-search-collapse {
  margin-top: -12px;
  border-top: 0;
}

.advanced-search-collapse :deep(.ant-collapse-content-box) {
  padding-top: 0 !important;
  padding-bottom: 0 !important;
}

.no-padding-panel :deep(.ant-collapse-header) {
  padding-bottom: 0 !important;
}

.advanced-search-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding-top: 8px;
  border-top: 1px dashed #f0f0f0;
}

/* 宽高和格式一行显示的样式 */
.dimensions-format-row {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  align-items: flex-start;
  margin-bottom: 8px;
}

.dimension-item {
  margin-right: 8px;
  margin-bottom: 8px;
  flex: 0 0 auto;
}

.dimension-container {
  display: flex;
  align-items: center;
}

.format-item {
  margin-right: 12px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  height: 32px;
}

.range-separator {
  margin: 0 4px;
}

.ant-form-item {
  margin-right: 12px;
  margin-bottom: 8px;
}

@media (max-width: 1200px) {
  .dimensions-format-row {
    flex-direction: column;
  }

  .dimension-item, .format-item {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .btn-form-item {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }

  .dimension-container {
    flex-direction: column;
    align-items: flex-start;
  }

  .dimension-container .a-slider {
    width: 100%;
    margin-left: 0;
    margin-top: 8px;
  }
}
</style>
