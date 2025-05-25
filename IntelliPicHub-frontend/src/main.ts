

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';

import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css'
import '@/access/index.ts';

// 导入开发者工具保护
import { initDevToolsProtection } from '@/utils/disableDevTools';
import { initAdvancedProtection } from '@/utils/advancedProtection';




const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(VueCropper)

// 初始化开发者工具保护（仅在生产环境生效）
initDevToolsProtection()

// 初始化高级保护（更强的保护措施）
initAdvancedProtection()

app.mount('#app')


