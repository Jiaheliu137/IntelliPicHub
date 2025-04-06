<template>
  <div id="userRegisterPage">
    <h2 class="title">IntelliPicHub - UserRegister</h2>
    <div class="desc">Intelligent Collaborative Cloud Image Library</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item
        name="userAccount"
        :rules="[{ required: true, message: 'Please input your user account!' }]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="please enter your account" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: 'Please input your password!' },
          { min: 8, message: 'The password length must be at least 8 characters.' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          placeholder="please enter your password"
        />
      </a-form-item>

      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: 'Please enter the confirmation password!' },
          { min: 8, message: 'The confirmation password length must be at least 8 characters.' },
        ]"
      >
        <a-input-password
          v-model:value="formState.checkPassword"
          placeholder="Please enter the confirmation password"
        />
      </a-form-item>

      <div class="tips">
        Already have an account?
        <RouterLink to="/user/login">Login now!</RouterLink>
      </div>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">Submit</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost, userRegisterUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

// 用于接收表单输入的值
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const loginUserStore = useLoginUserStore()

/**
 * 表单提交事件
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  // 校验两次输入的密码是否一致
  if (values.userPassword !== values.checkPassword) {
    message.error('The two passwords are inconsistent')
    return;
  }
  try {
    const res = await userRegisterUsingPost(values)
    // 注册成功，跳转到登录页
    if (res.data.code === 0 && res.data.data) {
      message.success('Register success')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('Register failed' + res.data.message)
    }
  } catch (error) {
    console.error('Register failed:', error)
    message.error('Register failed,please try later')
  }
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  color: #bbb;
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
