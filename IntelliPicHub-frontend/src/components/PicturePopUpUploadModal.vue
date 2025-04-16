<template>
  <a-modal
    v-model:visible="visible"
    title="Upload Picture"
    width="760px"
    :footer="null"
    :destroyOnClose="true"
  >
    <!-- 选择上传方式 -->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="File upload">
        <PictureUpload :picture="picture" :spaceId="props.spaceId" :onSuccess="onUploadSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="Url upload" force-render>
        <UrlPictureUpload :picture="picture" :spaceId="props.spaceId" :onSuccess="onUploadSuccess" />
      </a-tab-pane>
    </a-tabs>

    <!-- 图片信息表单 -->
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
        <a-button type="primary" html-type="submit" style="width: 100%">Add</a-button>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { listPictureTagCategoryUsingGet, editPictureUsingPost } from '@/api/pictureController.ts';
import PictureUpload from '@/components/PictureUpload.vue';
import UrlPictureUpload from '@/components/UrlPictureUpload.vue';

// 使用defineModel替代计算属性和emit
const visible = defineModel<boolean>('visible');
const props = defineProps<{
  spaceId?: number;
}>();
const emit = defineEmits(['success', 'cancel']);

// 组件内部状态
const uploadType = ref('file');
const picture = ref<API.PictureVO>();
const pictureForm = reactive<API.PictureEditRequest>({
  category: 'Other',
  tags: ['Other']
});

// 分类和标签选项
const categoryList = ref<string[]>([]);
const tagList = ref<string[]>([]);
const categoryOptions = ref<{value: string, label: string}[]>([]);
const tagOptions = ref<{value: string, label: string}[]>([]);

// 清除数据
watch(visible, (newVal) => {
  if (newVal) {
    // 弹窗打开时重置表单
    resetForm();
  }
});

const resetForm = () => {
  picture.value = undefined;
  pictureForm.name = '';
  pictureForm.introduction = '';
  pictureForm.category = 'Other';
  pictureForm.tags = ['Other'];
};

// 图片上传成功回调
const onUploadSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture;
  pictureForm.name = newPicture.name;
};

// 表单提交
const handleSubmit = async () => {
  try {
    if (!picture.value?.id) {
      message.error('Please upload a picture first');
      return;
    }

    // 确保有默认值
    if (!pictureForm.category || pictureForm.category.trim() === '') {
      pictureForm.category = 'Other';
    }

    if (!pictureForm.tags || pictureForm.tags.length === 0) {
      pictureForm.tags = ['Other'];
    }

    const res = await editPictureUsingPost({
      id: picture.value.id,
      ...pictureForm
    });

    if (res.data.code === 0) {
      message.success('Picture upload successful');
      visible.value = false; // 直接设置visible即可，不需要发射事件
      emit('success');
    } else {
      message.error('Failed to update picture info: ' + res.data.message);
    }
  } catch (e: unknown) {
    message.error('Failed to update picture info: ' + (e instanceof Error ? e.message : String(e)));
  }
};

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet();
    if (res.data.code === 0 && res.data.data) {
      // 保存数据
      categoryList.value = res.data.data.categoryList ?? [];
      tagList.value = res.data.data.tagList ?? [];

      // 转换为选项格式
      categoryOptions.value = categoryList.value.map(category => ({
        value: category,
        label: category
      }));

      tagOptions.value = tagList.value.map(tag => ({
        value: tag,
        label: tag
      }));
    }
  } catch (error: unknown) {
    message.error('Failed to load category and tags: ' + (error instanceof Error ? error.message : String(error)));
  }
};

onMounted(() => {
  getTagCategoryOptions();
});
</script>

<style scoped>
/* 可能需要的样式 */
</style>
