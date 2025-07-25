<template>
  <div class="container">
    <div class="back-home">
      <router-link to="/" class="back-btn">
        <i class="fas fa-arrow-left"></i>
        返回首页
      </router-link>
    </div>
    <div class="background">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="content">
      <div class="card">
        <div class="logo-wrapper">
          <img src="/logo.png" alt="MapleCoupon Logo" class="logo" />
          <div class="logo-glow"></div>
        </div>
        <h2 class="title">欢迎登录！🍁</h2>
        <p class="subtitle">登录您的MapleCoupon账号</p>

        <form @submit.prevent="handleLogin" class="form">
          <div class="form-group">
            <input type="text" v-model="username" required class="form-input" placeholder="用户名">
          </div>
          <div class="form-group">
            <input type="password" v-model="password" required class="form-input" placeholder="密码">
          </div>

          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <button type="submit" class="submit-button" :disabled="isLoading">
            <span v-if="!isLoading">登录</span>
            <span v-else class="loading">登录中...</span>
          </button>
        </form>

        <div class="footer">
          <router-link to="/register" class="register-link">
            还没有账号？立即注册
            <svg class="arrow-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
            </svg>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { couponAPI } from '../api/coupon.js'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/userStore'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const errorMessage = ref('')
const isLoading = ref(false)

const handleLogin = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const response = await couponAPI.login({
      username: username.value,
      password: password.value
    })
    console.log('登录响应:', response.data)
    // 适配新的响应数据结构
    if (response.data.success) {
      localStorage.setItem('token', response.data.data.token)
      localStorage.setItem('username', username.value)
      userStore.updateLoginStatus()
      setTimeout(() => {
        router.push('/admin/coupon-template')
      })
    } else {
      errorMessage.value = response.data?.message || '登录失败，请检查用户名和密码'
    }
  } catch (error) {
    errorMessage.value = error?.data?.message || '网络错误，请检查连接'
    console.error('登录错误详情:', error)
  } finally {
    isLoading.value = false
  }
}

</script>

<style scoped>
/* 添加返回首页按钮样式 */
.back-home {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  color: white;
  border-radius: 20px;
  text-decoration: none;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.4);
}

.back-btn i {
  font-size: 0.9rem;
}

.container {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.background {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8eaff 100%);
}

.blob {
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  filter: blur(40px);
  mix-blend-mode: multiply;
  opacity: 0.7;
}

.blob-1 {
  top: -40px;
  left: -40px;
  background: #bcd5ff;
  animation: blob 7s infinite;
}

.blob-2 {
  bottom: -40px;
  right: -40px;
  background: #c4c8ff;
  animation: blob 7s infinite 2s;
}

.blob-3 {
  top: 50%;
  left: 50%;
  background: #e0d5ff;
  animation: blob 7s infinite 4s;
}

.content {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 40px;
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  text-align: center;
  transition: transform 0.3s ease;
}

.logo-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
}

.logo {
  width: 80px;
  height: 80px;
  animation: float 3s ease-in-out infinite;
}

.logo-glow {
  position: absolute;
  inset: -4px;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  border-radius: 50%;
  filter: blur(8px);
  opacity: 0.3;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.subtitle {
  color: #4b5563;
  margin-bottom: 32px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.9);
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.error-message {
  padding: 12px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-radius: 12px;
  font-size: 0.875rem;
}

.submit-button {
  padding: 12px;
  background: linear-gradient(135deg, #3b82f6, #4f46e5);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
}

.submit-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.footer {
  margin-top: 24px;
}

.register-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s;
}

.register-link:hover {
  gap: 12px;
}

.arrow-icon {
  width: 16px;
  height: 16px;
  transition: transform 0.3s;
}

.register-link:hover .arrow-icon {
  transform: translateX(4px);
}

.loading {
  opacity: 0.7;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-10px);
  }
}

@keyframes blob {
  0% {
    transform: translate(0, 0) scale(1);
  }

  33% {
    transform: translate(30px, -50px) scale(1.1);
  }

  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }

  100% {
    transform: translate(0, 0) scale(1);
  }
}
</style>
