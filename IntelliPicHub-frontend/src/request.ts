import axios from 'axios'
import { message } from 'ant-design-vue'
import { DEV_HTTP_URL, PROD_HTTP_URL } from './config'

/**
 * 根据环境自动选择 API 地址
 * 生产环境使用 PROD_HTTP_URL，开发环境使用 DEV_HTTP_URL
 */
const getBaseURL = () => {
  // 使用 import.meta.env.PROD 判断是否为生产环境
  return import.meta.env.PROD ? PROD_HTTP_URL : DEV_HTTP_URL
}

// Create Axios instance
const myAxios = axios.create({
  baseURL: getBaseURL(),
  timeout: 60000,
  withCredentials: true,
})

// Global request interceptor
myAxios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// Global response interceptor
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // Not logged in
    if (data.code === 40100) {
      // If it's not a request to get user info and user is not currently on the login page,
      // then redirect to login page
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('Please login first')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  },
)

export default myAxios
