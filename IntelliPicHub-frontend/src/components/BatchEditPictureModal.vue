<template>
  <div class="batchEditPictureModal">
    <a-modal v-model:visible="visible" title="Batch Edit Picture" :footer="false" @cancel="closeModal">
      <a-typography-paragraph type=secondary>
        * Batch editing of images is only effective on the current page
      </a-typography-paragraph>
      <!--      批量创建表单-->

      <a-form
        name="batchEditPictureForm"
        layout="vertical"
        :model="batchEditPictureForm"
        @finish="handleSubmit"
      >

        <a-form-item name="category" label="Category">
          <a-auto-complete
            v-model:value="batchEditPictureForm.category"
            placeholder="Input categories"
            :options="optionsForCategory"
            allow-clear
          />
        </a-form-item>

        <a-form-item name="tags" label="Tag">
          <a-select
            v-model:value="batchEditPictureForm.tags"
            mode="tags"
            placeholder="Input tags"
            :options="optionsForTag"
            allow-clear
          >
          </a-select>
        </a-form-item>
        <a-form-item label="NameRule" name="nameRule">
          <a-input
            v-model:value="batchEditPictureForm.nameRule"
            placeholder="Please enter the naming rule. Enter name_{index} to generate automatically."
            allow-clear
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">Submit</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { defineProps, ref, withDefaults, defineExpose, reactive, onMounted, watch } from 'vue'
import {
  editPictureByBatchUsingPost,
  listPictureTagCategoryUsingGet
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'


const optionsForCategory = ref<{ value: string, label: string }[]>([])
const optionsForTag = ref<{ value: string, label: string }[]>([])


// 定义组件属性类型
interface Props {
  editPictureList: API.PictureVO[]
  spaceId: number | string
  categoryOptions?: {value: string, label: string}[]
  tagOptions?: {value: string, label: string}[]
  onSuccess: () => void
}

// 给组件指定初始值
const props = withDefaults(defineProps<Props>(), {})

// 当父组件传递选项时使用
watch(() => props.categoryOptions, (newVal) => {
  if (newVal && newVal.length > 0) {
    optionsForCategory.value = newVal;
  }
}, { immediate: true });

watch(() => props.tagOptions, (newVal) => {
  if (newVal && newVal.length > 0) {
    optionsForTag.value = newVal;
  }
}, { immediate: true });

// 控制弹窗可见性
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal
})


// 定义 picture 属性，存储上传的图片信息
const batchEditPictureForm = reactive<API.PictureEditByBatchRequest>({
  category: 'Other',
  tags: ['Other'],
  nameRule: 'flower_{index}'
})

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  // 如果父组件已经传了选项就不需要请求
  if ((props.categoryOptions && props.categoryOptions.length > 0) ||
      (props.tagOptions && props.tagOptions.length > 0)) {
    return;
  }

  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      // 转换为选项格式
      optionsForCategory.value = (res.data.data.categoryList ?? []).map(category => ({
        value: category,
        label: category
      }))

      optionsForTag.value = (res.data.data.tagList ?? []).map(tag => ({
        value: tag,
        label: tag
      }))
    }
  } catch (error: unknown) {
    message.error('获取分类和标签失败: ' + (error instanceof Error ? error.message : String(error)))
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

/**
 * 表单提交事件
 * @param values
 */
const handleSubmit = async (values: any) => {
  if (!props.editPictureList) {
    return
  }
  // 确保有默认值
  if (!values.category || values.category.trim() === '') {
    values.category = 'Other'
  }

  if (!values.tags || values.tags.length === 0) {
    values.tags = ['Other']
  }

  const res = await editPictureByBatchUsingPost({
    pictureIdList: props.editPictureList.map(picture => picture.id),
    spaceId: props.spaceId,
    ...values
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('Operation success')
    closeModal()
    props.onSuccess?.()
  } else {
    message.error('Operation failed')
  }
}


</script>

