<template>
  <div class="user-profile-container">
    <a-card title="User Profile" :bordered="false">
      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :sm="12" :md="8">
          <a-card class="profile-card" @click="showAvatarModal">
            <template #cover>
              <div class="avatar-container">
                <a-image
                  :src="loginUserStore.loginUser.userAvatar"
                  :preview-mask="false"
                  class="avatar-image"
                  style="width: 100px; height: 100px; border-radius: 50%;"
                />
                <div class="avatar-overlay">
                  <PictureOutlined style="font-size: 24px; color: #fff;" />
                </div>
              </div>
            </template>
            <a-card-meta title="Change Avatar" description="Upload a new profile picture" />
          </a-card>
        </a-col>

        <a-col :xs="24" :sm="12" :md="8">
          <a-card class="profile-card" @click="showPasswordModal">
            <template #cover>
              <div class="icon-container">
                <LockOutlined style="font-size: 40px; color: #1890ff;" />
              </div>
            </template>
            <a-card-meta title="Change Password" description="Update your account password" />
          </a-card>
        </a-col>

        <a-col :xs="24" :sm="12" :md="8">
          <a-card class="profile-card" @click="showInfoModal">
            <template #cover>
              <div class="icon-container">
                <EditOutlined style="font-size: 40px; color: #1890ff;" />
              </div>
            </template>
            <a-card-meta title="Edit Profile" description="Update username and profile description" />
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <!-- Avatar Modal -->
    <a-modal
      v-model:visible="avatarModalVisible"
      title="Change Avatar"
      @cancel="avatarModalVisible = false"
      :footer="null"
    >
      <div class="avatar-upload-container">
        <div class="current-avatar">
          <p>Current Avatar</p>
          <div class="avatar-wrapper">
            <a-image
              :src="loginUserStore.loginUser.userAvatar"
              :preview-mask="false"
              class="avatar-image"
              style="width: 100px; height: 100px; border-radius: 50%;"
            />
            <div class="upload-icon">
              <a-upload
                name="file"
                :showUploadList="false"
                :beforeUpload="beforeUpload"
                @change="handleAvatarChange"
              >
                <div class="plus-icon">
                  <PlusOutlined />
                </div>
              </a-upload>
            </div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- Image Cropper Component -->
    <AvatarCropper
      ref="avatarCropperRef"
      :imageUrl="tempAvatarUrl"
      @success="onCropSuccess"
    />

    <!-- Password Modal -->
    <a-modal
      v-model:visible="passwordModalVisible"
      title="Change Password"
      @ok="handlePasswordOk"
      :confirmLoading="confirmLoading"
      @cancel="passwordModalVisible = false"
      width="550px"
    >
      <a-form
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordFormRef"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item name="oldPassword" label="Current Password" has-feedback>
          <a-input-password
            v-model:value="passwordForm.oldPassword"
            placeholder="Enter current password"
            autocomplete="current-password"
          />
        </a-form-item>
        <a-form-item name="newPassword" label="New Password" has-feedback>
          <a-input-password
            v-model:value="passwordForm.newPassword"
            placeholder="Enter new password"
            autocomplete="new-password"
          />
        </a-form-item>
        <a-form-item name="checkPassword" label="Confirm New Password" has-feedback>
          <a-input-password
            v-model:value="passwordForm.checkPassword"
            placeholder="Confirm new password"
            autocomplete="new-password"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Profile Info Modal -->
    <a-modal
      v-model:visible="infoModalVisible"
      title="Edit Profile"
      @ok="handleInfoOk"
      :confirmLoading="confirmLoading"
      @cancel="infoModalVisible = false"
      width="600px"
    >
      <a-form
        :model="infoForm"
        :rules="infoRules"
        ref="infoFormRef"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="User ID">
          <a-input :value="loginUserStore.loginUser.id" disabled />
        </a-form-item>
        <a-form-item label="User Account">
          <a-input :value="loginUserStore.loginUser.userAccount || loginUserStore.loginUser.id" disabled />
        </a-form-item>
        <a-form-item name="userName" label="Username" has-feedback>
          <a-input v-model:value="infoForm.userName" placeholder="Enter username" />
        </a-form-item>
        <a-form-item name="userProfile" label="Profile Description">
          <a-textarea
            v-model:value="infoForm.userProfile"
            placeholder="Enter profile description"
            :rows="4"
            show-count
            :maxlength="200"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  PictureOutlined,
  LockOutlined,
  EditOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import {
  updatePasswordUsingPost,
  editUserInfoUsingPost,
  getLoginUserUsingGet
} from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import AvatarCropper from '@/components/AvatarCropper.vue'

const loginUserStore = useLoginUserStore()

// Shared states
const confirmLoading = ref(false)

// Avatar related states
const avatarModalVisible = ref(false)
const tempAvatarUrl = ref('')
const avatarFile = ref<File | null>(null)
const avatarCropperRef = ref()

// Password related states
const passwordModalVisible = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  checkPassword: ''
})
const passwordFormRef = ref()
const passwordRules = {
  oldPassword: [{ required: true, message: 'Please enter your current password', trigger: 'blur' }],
  newPassword: [
    { required: true, message: 'Please enter your new password', trigger: 'blur' },
    { min: 8, message: 'Password must be at least 8 characters', trigger: 'blur' }
  ],
  checkPassword: [
    { required: true, message: 'Please confirm your new password', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (value === passwordForm.newPassword) {
          return Promise.resolve()
        }
        return Promise.reject('The two passwords do not match')
      },
      trigger: 'blur'
    }
  ]
}

// Profile info related states
const infoModalVisible = ref(false)
const infoForm = reactive({
  userName: loginUserStore.loginUser.userName || '',
  userProfile: loginUserStore.loginUser.userProfile || ''
})
const infoFormRef = ref()
const infoRules = {
  userName: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 2, max: 20, message: 'Username must be between 2-20 characters', trigger: 'blur' }
  ]
}

// Page initialization
onMounted(async () => {
  // Refresh user information if needed
  try {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
      infoForm.userName = res.data.data.userName || ''
      infoForm.userProfile = res.data.data.userProfile || ''
    }
  } catch (error) {
    console.error('Failed to get user information', error)
  }
})

// Show modals
const showAvatarModal = () => {
  avatarModalVisible.value = true
  tempAvatarUrl.value = ''
  avatarFile.value = null
}

const showPasswordModal = () => {
  passwordModalVisible.value = true
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.checkPassword = ''
}

const showInfoModal = () => {
  infoModalVisible.value = true
  infoForm.userName = loginUserStore.loginUser.userName || ''
  infoForm.userProfile = loginUserStore.loginUser.userProfile || ''
}

// Handle avatar upload
const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('You can only upload JPG/PNG files!')
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('Image must be smaller than 10MB!')
  }
  return false // Prevent auto upload
}

const handleAvatarChange = (info: any) => {
  if (info.file) {
    avatarFile.value = info.file

    // Use FileReader for preview
    const reader = new FileReader()
    reader.onload = (e) => {
      tempAvatarUrl.value = e.target?.result as string
      // Open cropper modal immediately after image is loaded
      avatarCropperRef.value.openModal()
    }
    reader.readAsDataURL(info.file)
  }
}

// Handle cropped image from ImageCropper
const onCropSuccess = (newPicture: API.PictureVO) => {
  if (newPicture && newPicture.url) {
    // Update avatar in user store
    loginUserStore.loginUser.userAvatar = newPicture.url
    message.success('Avatar uploaded successfully')
    avatarModalVisible.value = false
  } else {
    message.error('Failed to process the image')
  }
}

// Handle password change
const handlePasswordOk = async () => {
  // Form validation
  try {
    await passwordFormRef.value.validate()

    confirmLoading.value = true
    // Call password update API
    const res = await updatePasswordUsingPost(passwordForm)
    if (res.data.code === 0) {
      message.success('Password changed successfully')
      passwordModalVisible.value = false
    } else {
      message.error(res.data.message || 'Password change failed')
    }
  } catch (error) {
    console.error('Password change failed', error)
  } finally {
    confirmLoading.value = false
  }
}

// Handle profile info change
const handleInfoOk = async () => {
  // Form validation
  try {
    await infoFormRef.value.validate()

    confirmLoading.value = true
    // Call profile update API
    const res = await editUserInfoUsingPost(infoForm)
    if (res.data.code === 0) {
      // Update locally stored user info
      loginUserStore.loginUser.userName = infoForm.userName
      loginUserStore.loginUser.userProfile = infoForm.userProfile

      message.success('Profile updated successfully')
      infoModalVisible.value = false
    } else {
      message.error(res.data.message || 'Profile update failed')
    }
  } catch (error) {
    console.error('Profile update failed', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style scoped>
.user-profile-container {
  padding: 24px;
}

.profile-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
}

.profile-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 30px 0;
  background: #f5f5f5;
  height: 160px;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.icon-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 30px 0;
  background: #f5f5f5;
  height: 160px;
}

.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.current-avatar {
  text-align: center;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.upload-icon {
  position: absolute;
  bottom: -4px;
  right: -4px;
  opacity: 1;
  transition: all 0.3s;
}

.plus-icon {
  background: #1890ff;
  border-radius: 50%;
  padding: 4px;
  color: white;
  width: 24px;
  height: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
}

.plus-icon:hover {
  background: #40a9ff;
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.avatar-image {
  border-radius: 50%;
  overflow: hidden;
}

/* 确保预览时的图片也是圆形的 */
:deep(.ant-image-preview-img) {
  border-radius: 50%;
}

:deep(.ant-card-meta-detail) {
  min-height: 60px;
}

:deep(.ant-upload.ant-upload-select-picture-card) {
  width: 128px;
  height: 128px;
  margin: 0;
}

:deep(.ant-upload-text) {
  margin-top: 8px;
  color: #666;
}
</style>
