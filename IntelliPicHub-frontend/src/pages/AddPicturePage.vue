<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? 'Edit picture' : 'Add picture' }}
    </h2>
    <!--    图片上传组件-->
    <PictureUpload :picture="picture" :onSuccess="onSuccess" />
    <!--    图片信息表单-->

    <a-form v-if="picture" name="pictureForm" layout="vertical" :model="pictureForm" @finish="handleSubmit">
      <a-form-item name="name" label="Name">
        <a-input
          v-model:value="pictureForm.name"
          placeholder="Input picture name"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="introduction" label="Introduction">
        <a-textarea v-model:value="pictureForm.introduction" placeholder="Input picture introduction"
                    :auto-size="{minRows:1,maxRows:4}" allow-clear />
      </a-form-item>


      <a-form-item name="category" label="Category">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="Input categories"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>

      <a-form-item name="tags" label="Tag">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="Input tags"
          :options="tagOptions"
          allow-clear
        >
        </a-select>
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">Submit</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>


<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet
} from '@/api/pictureController.ts'
import { useRoute, useRouter } from 'vue-router'

// 定义 picture 属性，存储上传的图片信息
const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})


// 定义 onSuccess 回调函数，在图片上传成功时被调用
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture //当子组件调用 onSuccess 并传递数据时，父组件中的 onSuccess 函数会被触发。父组件接收到数据后，更新自己的 picture 数据。
  pictureForm.name = newPicture.name
}

const router = useRouter()

// 获取老数据
const getOldPicture = async () => {
  // 获取到id
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({
      id
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}

onMounted(() => {
  getOldPicture()
})

/**
 * 表单提交事件
 * @param values
 */
const handleSubmit = async (values: API.PictureEditRequest) => {

  const pictureId = picture.value?.id
  if (!pictureId) {
    message.error('Please upload a picture first')
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    ...values
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('Operation success')
    // 跳转到图片详情页
    router.push({
      path: `/picture/${pictureId}`
    })
  } else {
    message.error('Operation failed')
  }
}

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

onMounted(() => {
  getTagCategoryOptions()
})

const route = useRoute()


</script>


<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}

</style>
