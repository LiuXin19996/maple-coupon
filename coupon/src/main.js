import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import './assets/main.css'
import axios from 'axios'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import apiClient from './api/coupon.js'
import '@fortawesome/fontawesome-free/css/all.css'
import '@fortawesome/fontawesome-free/css/all.min.css'
import scrollShow from './directives/scroll-show'

const app = createApp(App)
const pinia = createPinia()
app.config.warnHandler = () => null;
app.use(pinia)
// 挂载API模块
app.config.globalProperties.$apiClient = apiClient

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})
app.directive('scroll-show', scrollShow)
app.mount('#app')
