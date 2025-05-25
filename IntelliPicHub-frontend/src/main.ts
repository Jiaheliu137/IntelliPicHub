

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';

import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css'
import '@/access/index.ts';

// 禁用开发者工具
function disableDevTools() {
  // 禁用右键菜单
  document.addEventListener('contextmenu', (e) => {
    e.preventDefault();
    return false;
  });

  // 禁用F12、Ctrl+Shift+I、Ctrl+Shift+J、Ctrl+U等快捷键
  document.addEventListener('keydown', (e) => {
    // F12
    if (e.key === 'F12') {
      e.preventDefault();
      return false;
    }

    // Ctrl+Shift+I (开发者工具)
    if (e.ctrlKey && e.shiftKey && e.key === 'I') {
      e.preventDefault();
      return false;
    }

    // Ctrl+Shift+J (控制台)
    if (e.ctrlKey && e.shiftKey && e.key === 'J') {
      e.preventDefault();
      return false;
    }

    // Ctrl+U (查看源代码)
    if (e.ctrlKey && e.key === 'u') {
      e.preventDefault();
      return false;
    }

    // Ctrl+Shift+C (选择元素)
    if (e.ctrlKey && e.shiftKey && e.key === 'C') {
      e.preventDefault();
      return false;
    }

    // Ctrl+S (保存页面)
    if (e.ctrlKey && e.key === 's') {
      e.preventDefault();
      return false;
    }
  });

  // 检测开发者工具是否打开
  let devtools = {
    open: false,
    orientation: null
  };

  const threshold = 160;

  setInterval(() => {
    if (window.outerHeight - window.innerHeight > threshold ||
        window.outerWidth - window.innerWidth > threshold) {
      if (!devtools.open) {
        devtools.open = true;
        // 检测到开发者工具打开时的处理
        console.clear();
        document.body.innerHTML = '<div style="display:flex;justify-content:center;align-items:center;height:100vh;font-size:24px;color:#ff4d4f;">请关闭开发者工具后刷新页面</div>';
      }
    } else {
      devtools.open = false;
    }
  }, 500);

  // 禁用控制台输出
  if (typeof console !== 'undefined') {
    console.log = function() {};
    console.warn = function() {};
    console.error = function() {};
    console.info = function() {};
    console.debug = function() {};
  }

  // 检测调试器
  function detectDebugger() {
    const start = new Date().getTime();
    debugger;
    const end = new Date().getTime();
    if (end - start > 100) {
      // 检测到调试器
      window.location.reload();
    }
  }

  // 定期检测调试器
  setInterval(detectDebugger, 1000);
}

// 在生产环境中启用禁用开发者工具
if (import.meta.env.PROD) {
  disableDevTools();
}





const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(VueCropper)


app.mount('#app')


