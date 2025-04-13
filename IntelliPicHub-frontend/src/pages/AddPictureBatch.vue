<template>
  <div id="addPictureBathch">
    <h2 style="margin-bottom: 16px">
      Create pictures in batches
    </h2>


    <!--    图片信息表单-->
    <a-form
      name="formData"
      layout="vertical"
      :model="formData"
      @finish="handleSubmit"
    >
      <a-form-item name="searchText" label="searchText">
        <a-input v-model:value="formData.searchText" placeholder="Input searchText" allow-clear />
      </a-form-item>

      <div style="display: block; margin-bottom: 16px;">
        <a-space>
          <a-form-item name="first" label="First">

            <a-input-number
              v-model:value="formData.first"
              placeholder="Input the first"
              style="min-width:180px"
              :min="1"
              allow-clear
            />
          </a-form-item>

          <a-form-item name="count" label="Catch Count">

            <a-input-number
              v-model:value="formData.count"
              placeholder="Input the count"
              style="min-width:180px"
              :min="1"
              :max="35"
              allow-clear
            />
          </a-form-item>
          <div>Start from the {first} image in Bing image search and capture a total of {count} images.</div>
        </a-space>
      </div>

      <div style="display: block; margin-bottom: 16px;">
        <a-space>
          <a-form-item name="category" label="Category">
            <a-auto-complete
              v-model:value="formData.category"
              placeholder="Input categories"
              :options="categoryOptions"
              style="min-width:180px"
              allow-clear
            />
          </a-form-item>

          <a-form-item name="tags" label="Tag">
            <a-select
              v-model:value="formData.tags"
              mode="tags"
              placeholder="Input tags"
              :options="tagOptions"
              style="min-width:180px"
              allow-clear
            >
            </a-select>
          </a-form-item>
        </a-space>
      </div>

      <a-form-item name="namePrefix" label="namePrefix">
        <a-auto-complete
          v-model:value="formData.namePrefix"
          placeholder="Input namePrefix; cat,dog eg..."
          allow-clear
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
          Execute task
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>


<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { listPictureTagCategoryUsingGet, uploadPictureByBatchUsingPost } from '@/api/pictureController.ts'

// 定义 picture 属性，存储上传的图片信息
const formData = reactive<API.PictureUploadByBatchRequest>({
  count: 10,
  first: 1,
  searchText: 'Doraemon',
  category: 'other',
  tags: ["Other"]
})

const loading = ref(false)

const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

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

/**
 * 表单提交事件
 * @param values
 */
const handleSubmit = async (values: API.PictureUploadByBatchRequest) => {
  loading.value = true

  const res = await uploadPictureByBatchUsingPost({
    ...formData
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success(`Catch success,total:${res.data.data}`)
    // 新开一个页面跳转到主页
    window.open('/', '_blank')
  } else {
    message.error('Catch failed. ' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  getTagCategoryOptions()
})


</script>


<style scoped>
#addPictureBathch {
  max-width: 720px;
  margin: 0 auto;
}

</style>
