import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import ACCESS_ENUM from '@/access/accessEnum.ts'
import AddPicturePage from '@/pages/AddPicturePage.vue'
import PictureManagePage from '@/pages/admin/PictureManagePage.vue'
import PictureDetailPage from '@/pages/PictureDetailPage.vue'
import AddPictureBatch from '@/pages/AddPictureBatch.vue'
import SpaceManagePage from '@/pages/admin/SpaceManagePage.vue'
import AddSpacePage from '@/pages/AddSpacePage.vue'
import MySpacePage from '@/pages/MySpacePage.vue'
import SpaceDetailPage from '@/pages/SpaceDetailPage.vue'
import SearchByPicturePage from '@/pages/SearchByPicturePage.vue'
import SpaceAnalyzePage from '@/pages/SpaceAnalyzePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
      meta: {
        hideInMenu: false
      }
    },
    {
      path: '/picture/:id',
      name: 'Picture Information',
      component: PictureDetailPage,
      props: true,
      meta: {
        hideInMenu: false
      }
    },
    {
      path: '/user/login',
      name: 'user login',
      component: UserLoginPage,
      meta: {
        hideInMenu: true
      }
    },
    {
      path: '/user/register',
      name: 'user register',
      component: UserRegisterPage,
      meta: {
        hideInMenu: true
      }
    },
    {
      path: '/admin/userManage',
      name: 'user manage',
      component: UserManagePage,
      meta: {
        access: ACCESS_ENUM.ADMIN
      }
    },
    {
      path: '/admin/pictureManage',
      name: 'picture manage',
      component: PictureManagePage,
      meta: {
        access: ACCESS_ENUM.ADMIN
      }
    },
    {
      path: '/admin/spaceManage',
      name: 'space manage',
      component: SpaceManagePage,
      meta: {
        access: ACCESS_ENUM.ADMIN
      }
    },
    {
      path: '/add_picture',
      name: 'Add picture',
      component: AddPicturePage,
      meta: {
        hideInMenu: false
      }
    },
    {
      path: '/add_space',
      name: 'Add space',
      component: AddSpacePage,
      meta: {
        hideInMenu: false
      }
    },
    {
      path: '/my_space',
      name: 'My space',
      component: MySpacePage,
      meta: {
        hideInMenu: false
      }
    },
    {
      path: '/search_picture',
      name: 'Search by picture page',
      component: SearchByPicturePage,
      meta: {
        hideInMenu: false
      }
    },
    {
      path: '/space/:id',
      name: 'Space Information',
      component: SpaceDetailPage,
      props: true,
      meta: {
        hideInMenu: false
      }
    },
    {
      path: '/space_analyze',
      name: 'Space Analyze',
      component: SpaceAnalyzePage,
      props: true,
      meta: {
        hideInMenu: false
      }
    },
    {
      path: '/add_picture/batch',
      name: 'Add picture  in batches ',
      component: AddPictureBatch,
      meta: {
        access: ACCESS_ENUM.ADMIN,
        hideInMenu: false
      }
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
      meta: {
        hideInMenu: false
      }
    }
  ]
})

export default router
