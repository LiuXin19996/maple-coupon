import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
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

// 创建axios实例
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:10000',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization'
  },
  withCredentials: true
})

// 请求拦截器
http.interceptors.request.use(config => {
  console.log('Request:', config)
  
  // 允许注册和登录请求通过
  if (config.url.includes('/api/auth/user/register') || 
      config.url.includes('/api/auth/user/login') || config.url.includes('/')) {
    return config
  }

  // 添加认证头
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  if (token && username) {
    config.headers.token = token
    config.headers.username = username
  } else {
    router.push('/login')
    return Promise.reject(new Error('请先登录'))
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
http.interceptors.response.use(response => {
  console.log('Response:', response)
  return response
}, error => {
  if (error.response?.status === 401) {
    // 401未授权，清除token并跳转到登录页
    localStorage.removeItem('token')
    router.push('/login')
  }
  return Promise.reject(error)
})

const app = createApp(App)
app.use(store)

// 挂载axios到Vue实例
app.config.globalProperties.$axios = http
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
