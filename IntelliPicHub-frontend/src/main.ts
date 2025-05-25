

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';

import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css'
import '@/access/index.ts';

// 引入禁用开发者工具功能
import { initDisableDevTools } from '@/utils/disableDevTools';




const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(VueCropper)

// 初始化禁用开发者工具功能
initDisableDevTools()

app.mount('#app')


