<template>
  <div id="userLoginPage">
    <h2 class="title">IntelliPicHub - UserLogin</h2>
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

      <!--      <a-form-item name="remember" :wrapper-col="{ offset: 8, span: 16 }">-->
      <!--        <a-checkbox v-model:checked="formState.remember">Remember me</a-checkbox>-->
      <!--      </a-form-item>-->
      <div class="tips">
        No account?
        <RouterLink to="/user/register">Sign up now!</RouterLink>
      </div>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">Submit</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

// 用于接收表单输入的值
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const loginUserStore = useLoginUserStore()

/**
 * 表单提交事件
 * @param values
 */
const handleSubmit = async (values: API.UserLoginRequest) => {
  try {
    const res = await userLoginUsingPost(values)
    // 登陆成功，把登录态保存到全局状态中
    if (res.data.code === 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success('Login success')
      router.push({
        path: '/',
        replace: true,
      })
    } else {
      message.error('Login failed' + res.data.message)
    }
  } catch (error) {
    console.error('Login failed:', error)
    message.error('Login failed,please try later')
  }
}
</script>

<style scoped>
#userLoginPage {
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
