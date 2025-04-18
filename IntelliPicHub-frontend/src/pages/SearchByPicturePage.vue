<template>
  <div id="searchByPicturePage">
    <h2 style="margin-bottom: 16px">
      Search by Picture
    </h2>
    <h3 style="margin-bottom: 16px">
      Original Picture
    </h3>
    <!--    单张图片-->
    <a-card hoverable :bordered="false" class="picture-card">
      <template #cover>
        <img
          :alt="picture.name"
          :src="picture.thumbnailUrl ?? picture.url"
          class="picture-image"
        />
      </template>
    </a-card>
    <h3 style="margin: 16px 0">
      Similar Pictures
    </h3>

    <!--    图片结果列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <a :href="picture.fromUrl" target="_blank">
            <!--          单张图片-->
            <a-card hoverable>
              <template #cover>
                <img
                  :alt="picture.name"
                  :src="picture.thumbUrl??picture.url"
                  style="height: 180px; object-fit: cover"
                />
              </template>
            </a-card>
          </a>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  searchPictureByPictureUsingPost,
  getPictureVoByIdUsingGet
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'


const route = useRoute()
const picture = ref<API.PictureVO>({})
const pictureId = computed(() => {
  return route.query?.pictureId
})
const dataList = ref<API.ImageSearchResult[]>([])
const loading=ref<boolean>(true)

// 获取原图信息
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: pictureId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('Failed to get original picture, ' + res.data.message)
    }
  } catch (e: any) {
    message.error('Failed to get original picture, ' + e.message)
  }
}

// 获取搜图结果
const fetchResultData = async () => {
  loading.value=true
  try {
    const res = await searchPictureByPictureUsingPost({
      pictureId: pictureId.value
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data
    } else {
      message.error('Failed to get similar picture, ' + res.data.message)
    }
  } catch (e: any) {
    message.error('Failed to get similar picture, ' + e.message)
  }
  loading.value=false
}

// 页面加载时请求一次
onMounted(() => {
  fetchPictureDetail()
  fetchResultData()
})


</script>

<style scoped>
#searchByPicturePage {
  margin-bottom: 16px;
}

.picture-card {
  display: inline-block;
  background: transparent;
}

.picture-image {
  max-width: 100%;
  height: auto;
  object-fit: contain;
  display: block;
}
</style>
