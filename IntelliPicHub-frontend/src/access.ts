// import router from '@/router'
// import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
// import { message } from 'ant-design-vue'
//
// // 是否首次获取用户登录信息
// let firstFetchLoginUser = true
//
// // beforeEach是 Vue Router 的全局前置守卫，它会在路由切换之前被调用。它允许你对每次路由跳转进行处理，比如权限检查、数据加载等。
// // 因此在前面要设置一个变量来判断是不是首次获取用户登录信息
// /**
//  * 全局权限校验，每次切换时都会执行
//  */
// router.beforeEach(async (to, from, next) => {
//   const loginUserStore = useLoginUserStore()
//   let loginUser = loginUserStore.loginUser
//   // 确保页面刷新时，首次加载时，能等待后端返回用户信息后，再校验权限
//   if (firstFetchLoginUser) {
//     await loginUserStore.fetchLoginUser()
//     loginUser = loginUserStore.loginUser
//     firstFetchLoginUser = false
//   }
//   const toUrl = to.fullPath
//   // 自定义权限校验逻辑，管理员才能访问/admin开头的页面
//   if (toUrl.startsWith('/admin')) {
//     if (!loginUser || loginUser.userRole !== 'admin') {
//       message.error('No permission to access this page')
//       // 返回/user/login页面，然后在用户登陆后返回到to.fullPath页面
//       next(`/user/login?redirect=${to.fullPath}`)
//       return
//     }
//   }
//   next() // 放行
// })
