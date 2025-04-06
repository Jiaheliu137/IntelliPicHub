import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/userController.ts'

/**
 * 存储用户信息的状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: 'unLogin'
  })

  /**
   * 远程获取用户登陆
   */
  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
    // test user login,auto login after 3s
    //   setTimeout(()=>{
    //     loginUser.value = {userName: 'test user',id:1}
    //   },3000)
  }

  /**
   * 设置登录用户
   * @param {any} newLoginUser - 新的用户信息
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
