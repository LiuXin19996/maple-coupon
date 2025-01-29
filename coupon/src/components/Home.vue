<template>
  <div class="container">
    <!-- 导航栏优化 -->
    <nav class="navbar" :class="{ 'navbar-scrolled': isScrolled }">
      <div class="nav-content">
        <div class="nav-left">
          <div class="logo-container">
            <img src="/logo.png" alt="Logo" class="nav-logo" />
            <div class="logo-shine"></div>
          </div>
          <span class="nav-brand">MapleCoupon</span>
        </div>
        <div class="nav-right">
          <div class="nav-links">
            <router-link v-for="link in navLinks" :key="link.href" :to="link.href" class="nav-link"
              @mouseover="handleLinkHover" @mouseleave="handleLinkLeave">
              <span class="nav-link-content">{{ link.text }}</span>
              <span class="nav-link-indicator"></span>
              <div class="nav-link-glow"></div>
            </router-link>
          </div>
          <div class="auth-buttons" v-if="!isLoggedIn">
            <router-link :to="{ name: 'Login' }" class="auth-button login">
              登录
            </router-link>
            <router-link :to="{ name: 'Register' }" class="auth-button register">
              注册
            </router-link>
          </div>
          <div v-else class="user-info">
            <div class="user-welcome">
              <span class="user-icon">🤯</span>
              <span class="username">{{ username }}</span>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- 动态背景增强 -->
    <div class="background">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
      <div class="grid-overlay"></div>
      <div class="particle-container">
        <div v-for="n in 20" :key="n" class="particle" :style="{ '--delay': `${n * 0.3}s` }">
        </div>
      </div>
    </div>

    <!-- Hero Section 优化 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-text" v-scroll-show>
          <div class="badge">
            <span class="badge-icon">⚡</span>
            <span class="badge-text">基于Spring Cloud微服务架构</span>
          </div>
          <h1 class="hero-title">
            <span class="gradient-text">分布式优惠券系统</span>
            <br />
            <span>解决方案</span>
          </h1>
          <p class="hero-subtitle">
            高性能 · 高可用 · 可扩展
            <span class="highlight">10万级并发处理能力</span>
          </p>
          <div class="hero-buttons">
            <button class="cta-button primary" @click="goToAdmin">
              <span>快速使用</span>
              <div class="button-glow"></div>
            </button>
          </div>
        </div>

        <!-- 统计数字动画优化 -->
        <div class="hero-stats" v-scroll-show>
          <div v-for="(stat, index) in stats" :key="index" class="stat-card" :style="{ '--delay': `${index * 0.1}s` }">
            <div class="stat-icon">{{ stat.icon }}</div>
            <div class="stat-content">
              <div class="stat-number">
                <counter-up :end-value="stat.value" :duration="2000" :decimals="stat.decimals || 0" />
                <span v-if="stat.suffix">{{ stat.suffix }}</span>
              </div>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 详细介绍部分 -->
    <section class="common-section" v-scroll-show>
      <div class="section-header">
        <span class="section-title">企业级分布式架构设计</span>
        <h2 class="section-badge">核心技术架构</h2>
        <p class="section-subtitle">
          采用Spring Cloud Alibaba微服务框架，整合RocketMQ、Redis、ShardingSphere等中间件，
          实现高性能、高可用的分布式系统架构。配合Canal数据同步，确保数据一致性，满足企业级应用的严苛需求。
        </p>
      </div>

      <div class="feature-cards-grid">
        <div class="feature-card" v-for="(feature, index) in introFeatures" :key="index"
          :style="{ '--delay': `${index * 0.15}s` }">
          <div class="feature-icon-wrapper">
            <span class="feature-icon">{{ feature.icon }}</span>
            <div class="icon-background"></div>
          </div>
          <h3>{{ feature.title }}</h3>
          <p>{{ feature.description }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import CounterUp from './CounterUp.vue'

const username = ref(localStorage.getItem('username') || '')
const isLoggedIn = computed(() => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  return token && username
})
const router = useRouter()
const isScrolled = ref(false)

const navLinks = [
  { href: '/features', text: '技术特性' },
  { href: '/about', text: '关于项目' }
]

const stats = [
  { icon: '🏆', value: 100000, label: '并发处理能力', suffix: '/s' },
  { icon: '🌶️', value: 50, label: '数据同步延迟', suffix: 'ms' },
  { icon: '💣', value: 99.99, label: '系统可用性', suffix: '%', decimals: 2 }
]

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  const particles = document.querySelectorAll('.particle')
  particles.forEach(particle => {
    particle.style.left = `${Math.random() * 100}%`
    particle.style.top = `${Math.random() * 100}%`
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const goToAdmin = () => {
  router.push('/admin')
}

// 添加滚动显示指令
const vScrollShow = {
  mounted: (el) => {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          el.classList.add('is-visible')
        }
      })
    }, {
      threshold: 0.1
    })
    observer.observe(el)
  }
}

// 添加介绍部分的特性数据
const introFeatures = [
  {
    icon: '🔨',
    title: '分库分表设计',
    description: '支持海量数据处理，提供灵活的扩展能力'
  },
  {
    icon: '⚖️',
    title: '消息队列削峰',
    description: '处理高并发场景，确保系统稳定性'
  },
  {
    icon: '🚀',
    title: '多级缓存策略',
    description: '优化访问性能，提升系统响应速度'
  },
  {
    icon: '⚡',
    title: '高性能架构',
    description: '基于分库分表的架构设计，集成Redis多级缓存，可承受10万级并发请求，保障业务高峰平稳运行。',
    highlighted: false
  },
  {
    icon: '🔄',
    title: '分布式设计',
    description: '采用Spring Cloud微服务架构，通过RocketMQ实现消息驱动，Redisson处理分布式锁，确保数据一致性。',
    highlighted: false
  },
  {
    icon: '🛡️',
    title: '可靠性保障',
    description: 'Canal数据同步机制，配合多级缓存策略，支持分布式事务，实现高可用及数据可靠性。',
    highlighted: false
  },
  {
    icon: '📊',
    title: '性能监控',
    description: '完整的链路追踪和性能监控，支持全方位系统诊断，快速定位性能瓶颈。',
    highlighted: false
  },
  {
    icon: '🔒',
    title: '安全机制',
    description: 'Spring Security权限控制，JWT认证，配合Sentinel实现流量防护，全方位保障系统安全。',
    highlighted: false
  },
  {
    icon: '🔧',
    title: '便捷开发',
    description: '遵循阿里巴巴编码规范，完善的开发文档，模块化设计，支持快速二次开发。',
    highlighted: false
  }
]

const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-CN').format(Math.round(num))
}

const handleLinkHover = (event) => {
  const indicator = event.currentTarget.querySelector('.nav-link-indicator')
  const glow = event.currentTarget.querySelector('.nav-link-glow')
  if (indicator && glow) {
    indicator.style.transform = 'scaleX(1)'
    glow.style.opacity = '1'
  }
}

const handleLinkLeave = (event) => {
  const indicator = event.currentTarget.querySelector('.nav-link-indicator')
  const glow = event.currentTarget.querySelector('.nav-link-glow')
  if (indicator && glow) {
    indicator.style.transform = 'scaleX(0)'
    glow.style.opacity = '0'
  }
}
</script>

<style scoped>
/* 基础重置和容器样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.container {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  max-width: 1200px;
  margin: 0 auto;
}

/* 导航栏样式 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(209, 213, 219, 0.3);
  transition: all 0.4s ease;
}

.navbar-scrolled {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* 基础布局样式 */
.nav-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 2rem;
  max-width: 1400px;
  margin: 0 auto;
  height: 70px;
}

/* 导航元素样式 */
.nav-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 2rem;
}

/* Logo样式 */
.logo-container {
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  padding: 0.5rem;
  background: rgba(59, 130, 246, 0.1);
  transition: transform 0.3s ease;
}

.logo-container:hover {
  transform: scale(1.05);
}

.nav-logo {
  height: 32px;
  width: auto;
  position: relative;
  z-index: 1;
}

.logo-shine {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 45%,
    rgba(255, 255, 255, 0.5) 50%,
    transparent 55%
  );
  transform: translate(-100%, -100%);
  animation: shine 3s infinite;
}

.nav-brand {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-left: 1rem;
  letter-spacing: -0.01em;
}

/* 导航链接样式 */
.nav-links {
  display: flex;
  gap: 2.5rem;
  align-items: center;
}

.nav-link {
  position: relative;
  text-decoration: none;
  padding: 0.5rem 0;
  font-weight: 500;
  font-size: 0.95rem;
  letter-spacing: 0.01em;
  transition: all 0.3s ease;
}

.nav-link-content {
  position: relative;
  z-index: 1;
  background: linear-gradient(
    to right,
    #4b5563,
    #3b82f6
  );
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  transition: all 0.3s ease;
}

/* 悬停指示器 */
.nav-link-indicator {
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 2px;
}

/* 发光效果 */
.nav-link-glow {
  position: absolute;
  inset: -4px -8px;
  background: radial-gradient(
    circle at center,
    rgba(59, 130, 246, 0.15),
    transparent 70%
  );
  border-radius: 8px;
  opacity: 0;
  transition: all 0.4s ease;
  pointer-events: none;
}

/* 活跃状态样式 */
.router-link-active .nav-link-content {
  color: #3b82f6;
  font-weight: 600;
}

.router-link-active .nav-link-indicator {
  transform: scaleX(1);
  background: #3b82f6;
}

/* 悬停效果 */
.nav-link:hover .nav-link-content {
  color: #3b82f6;
}

.nav-link:hover .nav-link-glow {
  opacity: 1;
}

/* Hero section样式 */
.hero-section {
  min-height: calc(100vh - 80px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 120px 0 40px;
}

.hero-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 4rem 2rem;
  border-radius: 24px;
  background: linear-gradient(135deg, #3b82f6, #4f46e5);
  color: white;
  position: relative;
  overflow: hidden;
}

/* 背景效果 */
.background {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8eaff 100%);
  z-index: -1;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.6;
}

.blob-1 {
  background: #3b82f6;
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
}

.blob-2 {
  background: #4f46e5;
  width: 400px;
  height: 400px;
  bottom: -150px;
  right: -150px;
}

.blob-3 {
  background: #2563eb;
  width: 250px;
  height: 250px;
  top: 40%;
  left: 60%;
}

/* 数据卡片样式 */
.data-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
}

.card-highlight {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
}

.card-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 12px;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #64748b;
}

.card-trend {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-trend.up {
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
}

.card-trend.down {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* 仪表盘样式 */
.mock-dashboard {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.3s ease;
}

.dashboard-header {
  padding: 16px 24px;
  background: linear-gradient(to right, #f8fafc, #f1f5f9);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .nav-content {
    padding: 0.75rem 1rem;
  }
  
  .hero-content {
    padding: 2rem 1rem;
  }
  
  .data-cards {
    grid-template-columns: 1fr;
  }
  
  .header-tabs {
    display: none;
  }
}

/* 必要的动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

[v-scroll-show] {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

[v-scroll-show].is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* 高级视觉效果 */
.gradient-text {
  background: linear-gradient(135deg, #3b82f6 0%, #4f46e5 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: transparent;
  background-size: 200% auto;
  animation: textShine 4s linear infinite;
}

.hero-title {
  font-size: 4rem;
  line-height: 1.2;
  margin: 1.5rem 0;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.hero-subtitle {
  font-size: 1.5rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 2rem;
}

.badge {
  display: inline-flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  backdrop-filter: blur(10px);
}

.badge-icon {
  margin-right: 0.5rem;
}

.badge-text {
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 0.02em;
  background: linear-gradient(to right, #fff, rgba(255, 255, 255, 0.8));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 动态按钮效果 */
.cta-button {
  position: relative;
  padding: 1rem 2rem;
  border-radius: 12px;
  border: none;
  font-weight: 600;
  font-size: 1.1rem;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
}

.cta-button.primary {
  background: linear-gradient(135deg, #3b82f6, #4f46e5);
  color: white;
}

.button-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(255,255,255,0.8) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.cta-button:hover .button-glow {
  opacity: 0.4;
}

/* 统计卡片样式 */
.hero-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-top: 3rem;
}

.stat-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 1.5rem;
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.6s forwards;
  animation-delay: var(--delay);
}

/* 特性卡片样式 */
.feature-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2rem;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.6s forwards;
  animation-delay: var(--delay);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.feature-icon-wrapper {
  position: relative;
  width: 60px;
  height: 60px;
  margin-bottom: 1.5rem;
}

.feature-icon {
  position: relative;
  z-index: 1;
  font-size: 2rem;
}

.icon-background {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(79, 70, 229, 0.1));
  border-radius: 12px;
  transform: rotate(-5deg);
}

/* 粒子动画 */
.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(59, 130, 246, 0.3);
  border-radius: 50%;
  pointer-events: none;
  animation: floatParticle 20s linear infinite;
  animation-delay: var(--delay);
}

@keyframes floatParticle {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(calc(100vw - 100%), calc(100vh - 100%));
  }
}

/* 响应式优化 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-subtitle {
    font-size: 1.2rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }
}

/* 新增网格背景效果 */
.grid-overlay {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.2;
}

/* 文本样式增强 */
.section-title {
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #1a365d 0%, #2563eb 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 1.5rem;
  line-height: 1.2;
  letter-spacing: -0.02em;
  text-shadow: 0 2px 10px rgba(37, 99, 235, 0.1);
}

.section-subtitle {
  font-size: 1.2rem;
  color: #64748b;
  margin-bottom: 3rem;
  line-height: 1.6;
  max-width: 600px;
  margin: 0 auto 3rem;
}

.intro-paragraph {
  font-size: 1.1rem;
  line-height: 1.8;
  color: #475569;
  margin-bottom: 2rem;
  text-align: justify;
  letter-spacing: 0.01em;
}

.feature-list li {
  font-size: 1.1rem;
  color: #334155;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.highlight {
  background: linear-gradient(120deg, rgba(56, 189, 248, 0.2) 0%, rgba(59, 130, 246, 0.2) 100%);
  padding: 0.2em 0.5em;
  border-radius: 4px;
  font-weight: 500;
  color: #bfdbea;
  margin: 0 0.2em;
}

/* 卡片文本样式 */
.feature-card h3 {
  font-size: 1.4rem;
  font-weight: 700;
  margin-bottom: 1rem;
  background: linear-gradient(135deg, #1e293b, #334155);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.01em;
}

.feature-card p {
  font-size: 1rem;
  line-height: 1.7;
  color: #64748b;
  margin-bottom: 1.5rem;
  text-align: justify;
}

/* 统计数字样式 */
.stat-number {
  font-size: 2.4rem;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 0.5rem;
  background: linear-gradient(to right, #fff, rgba(255, 255, 255, 0.8));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  letter-spacing: 0.02em;
}

/* 文本动画效果 */
@keyframes textShine {
  0% {
    background-position: 0% 50%;
  }
  100% {
    background-position: 100% 50%;
  }
}

/* 响应式文本调整 */
@media (max-width: 768px) {
  .section-title {
    font-size: 2rem;
  }
  
  .intro-paragraph {
    font-size: 1rem;
    line-height: 1.6;
  }
  
  .feature-list li {
    font-size: 1rem;
  }
  
  .stat-number {
    font-size: 2rem;
  }
}

/* 特性卡片网格布局优化 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);  /* 三列布局 */
  grid-template-rows: repeat(2, auto);    /* 两行布局 */
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.feature-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2rem;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.6s forwards;
  animation-delay: var(--delay);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.feature-card-highlighted {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(79, 70, 229, 0.1));
  border: 1px solid rgba(59, 130, 246, 0.2);
}

/* 响应式布局优化 */
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr); /* 平板设备显示两列 */
    gap: 1.5rem;
    padding: 1.5rem;
  }
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr; /* 手机设备显示单列 */
    gap: 1rem;
    padding: 1rem;
  }
  
  .feature-card {
    padding: 1.5rem;
  }
}

/* 特性部分容器样式优化 */
.features-section {
  padding: 4rem 2rem;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0.8));
  position: relative;
}

.section-header {
  text-align: center;
  max-width: 800px;
  margin: 0 auto 4rem;
  padding: 0 1rem;
}

/* 介绍部分特性卡片样式 */
.intro-features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-top: 2rem;
}

.intro-feature-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.6s forwards;
  animation-delay: var(--delay);
}

.intro-feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(79, 70, 229, 0.05));
}

.intro-feature-icon {
  font-size: 1.8rem;
  padding: 1rem;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(79, 70, 229, 0.1));
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.intro-feature-content {
  flex: 1;
}

.intro-feature-content h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #1e293b;
  background: linear-gradient(135deg, #1e293b, #334155);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.intro-feature-content p {
  font-size: 0.95rem;
  color: #64748b;
  line-height: 1.5;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .intro-features-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .intro-feature-card {
    padding: 1.2rem;
  }
  
  .intro-feature-icon {
    font-size: 1.5rem;
    padding: 0.8rem;
  }
}

/* 统一section样式 */
.common-section {
  padding: 6rem 2rem;
  position: relative;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0.8));
}

.section-header {
  text-align: center;
  max-width: 800px;
  margin: 0 auto 4rem;
  padding: 0 1rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #1a365d 0%, #2563eb 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 1rem;
}

.section-badge {
  font-size: 1.8rem;
  color: #1e293b;
  margin: 1rem 0;
  font-weight: 700;
}

.section-subtitle {
  font-size: 1.2rem;
  color: #64748b;
  line-height: 1.8;
  max-width: 700px;
  margin: 1rem auto 0;
}

/* 统一卡片网格样式 */
.feature-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

/* 统一卡片样式 */
.feature-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.6s forwards;
  animation-delay: var(--delay);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(79, 70, 229, 0.05));
}

.feature-card-highlighted {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(79, 70, 229, 0.1));
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.feature-icon-wrapper {
  position: relative;
  width: 60px;
  height: 60px;
  margin-bottom: 0.5rem;
}

.feature-icon {
  position: relative;
  z-index: 1;
  font-size: 2rem;
}

.icon-background {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(79, 70, 229, 0.1));
  border-radius: 12px;
  transform: rotate(-5deg);
}

.feature-card h3 {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.feature-card p {
  font-size: 1rem;
  line-height: 1.7;
  color: #64748b;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .common-section {
    padding: 4rem 1rem;
  }

  .section-title {
    font-size: 2rem;
  }

  .section-badge {
    font-size: 1.5rem;
  }

  .section-subtitle {
    font-size: 1.1rem;
  }

  .feature-cards-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .feature-card {
    padding: 1.5rem;
  }
}

/* Logo动画 */
@keyframes shine {
  0% {
    transform: translate(-100%, -100%) rotate(45deg);
  }
  80%, 100% {
    transform: translate(100%, 100%) rotate(45deg);
  }
}

/* 认证按钮样式 */
.auth-buttons {
  display: flex;
  gap: 1rem;
  margin-left: 2rem;
}

.auth-button {
  padding: 0.5rem 1.5rem;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  text-decoration: none;
  white-space: nowrap;
}

.auth-button.login {
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
}

.auth-button.login:hover {
  background: rgba(59, 130, 246, 0.2);
}

.auth-button.register {
  color: white;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.auth-button.register:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-left: 1rem;
}

.user-welcome {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 8px;
  color: #3b82f6;
}

.user-icon {
  font-size: 1.2rem;
}

.username {
  font-weight: 500;
  font-size: 0.95rem;
}

.logout-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  font-weight: 500;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2);
}

.logout-icon {
  font-size: 1.1rem;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .user-info {
    margin-left: 0.5rem;
  }

  .user-welcome {
    padding: 0.4rem 0.8rem;
  }

  .logout-button {
    padding: 0.4rem 0.8rem;
  }
}
</style>