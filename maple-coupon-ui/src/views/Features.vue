<template>
  <div class="features-page">
    <!-- 添加返回首页按钮 -->
    <div class="back-home">
      <router-link to="/" class="back-btn">
        <i class="fas fa-arrow-left"></i>
        返回首页
      </router-link>
    </div>

    <!-- SVG 滤镜移到这里 -->
    <svg class="filters">
      <defs>
        <filter id="gooey">
          <feGaussianBlur in="SourceGraphic" stdDeviation="10" result="blur" />
          <feColorMatrix 
            in="blur" 
            type="matrix"
            values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 18 -7" 
            result="gooey" />
        </filter>
      </defs>
    </svg>

    <header class="page-header">
      <div class="header-content">
        <h1 class="gradient-text">MapleCoupon系统特性</h1>
        <p class="header-subtitle">高性能分布式优惠券系统解决方案</p>
        <div class="header-decoration">
          <div class="decoration-circle"></div>
        </div>
      </div>
      <div class="header-background">
        <div class="bg-pattern"></div>
      </div>
    </header>

    <div class="features-content">
      <div v-for="(feature, index) in features" 
           :key="index"
           class="feature-section"
           v-scroll-show>
        <div class="feature-details">
          <span class="feature-badge">{{ feature.badge }}</span>
          <h2>{{ feature.title }}</h2>
          <p>{{ feature.description }}</p>
          <ul class="feature-list">
            <li v-for="(point, idx) in feature.points" 
                :key="idx"
                class="feature-point">
              <i class="fas fa-check-circle"></i>
              {{ point }}
            </li>
          </ul>
        </div>
        <div class="feature-animation">
          <div class="animation-container">
            <div class="animation-glow"></div>
            <div class="animation-particles"></div>
            <div :class="`animation-${index + 1}`">
              <div class="animation-element"></div>
            </div>
          </div>
        </div>
      </div>

      <section class="comparison-section" v-scroll-show>
        <h2 class="comparison-title">系统对比</h2>
        <div class="comparison-subtitle">全方位的系统功能对比分析</div>
        <div class="comparison-grid">
          <div v-for="(comp, index) in comparisons" 
               :key="index"
               class="comparison-card"
               :class="{ 'recommended': comp.recommended }">
            <div class="comparison-header">
              <div class="header-content">
                <h3>{{ comp.title }}</h3>
                <div class="header-decoration"></div>
              </div>
              <span v-if="comp.recommended" class="recommended-badge">
                <i class="fas fa-crown"></i>
                推荐方案
              </span>
            </div>
            <ul class="comparison-list">
              <li v-for="(point, idx) in comp.points" 
                  :key="idx"
                  :class="{ 'highlight': comp.recommended }">
                <i :class="[
                  'feature-icon',
                  comp.recommended ? 'fas fa-check-circle' : 'fas fa-minus-circle'
                ]"></i>
                <span class="point-text">{{ point }}</span>
              </li>
            </ul>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const features = ref([
  {
    badge: '高性能',
    title: '强大的并发处理能力',
    description: '系统能够承受近十万次查询和分发请求的高并发压力，采用先进的分布式架构设计。',
    points: [
      '支持高并发优惠券查询和分发',
      '开源框架Easy Excel优化访问性能',
      'RocketMQ消息队列削峰填谷',
      'Redis分布式缓存支持'
    ]
  },
  {
    badge: '架构设计',
    title: '先进的技术架构',
    description: '基于Spring Cloud Alibaba构建的微服务架构，采用最新技术栈实现。',
    points: [
      'Spring Boot 3.0.7 基础框架',
      'Spring Cloud Alibaba 微服务支持',
      'ShardingSphere 分库分表方案',
      'Nacos 服务注册与发现、配置中心'
    ]
  },
  {
    badge: '业务功能',
    title: '全面的券类支持',
    description: '支持多种类型优惠券管理，包括平台券、店铺券、折扣券、满减券、立减券等。',
    points: [
      '多样化的优惠券类型',
      '灵活的领券使用限制',
      '完整的券生命周期管理',
      '精细化的权限控制'
    ]
  }
])

const comparisons = ref([
  {
    title: '传统券系统',
    points: [
      '单一数据库架构',
      '有限的并发处理能力',
      '简单的券类型支持',
      '基础的管理功能',
      '固定的业务流程'
    ],
    recommended: false
  },
  {
    title: 'MapleCoupon系统',
    points: [
      '分布式微服务架构',
      '十万级并发处理能力',
      '丰富的券类型支持',
      '完善的管理后台',
      '灵活的扩展能力'
    ],
    recommended: true
  }
])
</script>

<style scoped>
.features-page {
  padding-top: 80px;
  background: linear-gradient(to bottom, #f8fafc, #fff);
}

.page-header {
  position: relative;
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: 60px;
}

.header-content {
  text-align: center;
  z-index: 1;
  padding: 0 20px;
}

.gradient-text {
  font-size: 4rem;
  font-weight: 800;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 1.5rem;
  position: relative;
  text-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  animation: titleFloat 3s ease-in-out infinite;
}

.header-subtitle {
  font-size: 1.4rem;
  color: #64748b;
  margin-top: 1rem;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.header-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(139, 92, 246, 0.1));
  top: -150px;
  right: -150px;
  animation: rotate 20s linear infinite;
}


.features-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 20px;
}

.feature-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  margin-bottom: 100px;
  align-items: center;
}

.feature-section:nth-child(even) {
  direction: rtl;
}

.feature-section:nth-child(even) .feature-details {
  direction: ltr;
}

.feature-animation {
  width: 100%;
  height: 400px;
  border-radius: 24px;
  overflow: hidden;
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(59, 130, 246, 0.1);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    inset 0 0 30px rgba(59, 130, 246, 0.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-animation:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 
    0 30px 60px rgba(59, 130, 246, 0.15),
    inset 0 0 50px rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
}

.animation-container {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at center, rgba(59, 130, 246, 0.02) 0%, transparent 70%);
}

.animation-element {
  position: absolute;
}

.animation-glow {
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at center, rgba(59, 130, 246, 0.1) 0%, transparent 50%);
  animation: glowPulse 4s ease-in-out infinite;
}

.animation-particles {
  position: absolute;
  width: 100%;
  height: 100%;
  filter: url(#gooey);
}

.animation-particles::before,
.animation-particles::after {
  content: '';
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.3);
  animation: particleFloat 6s ease-in-out infinite;
}

.animation-particles::after {
  animation-delay: -3s;
}

/* 动画1：数据流动效果 */
.animation-1 .animation-element {
  width: 70%;
  height: 70%;
  background: linear-gradient(45deg, #3b82f6, #8b5cf6);
  border-radius: 20px;
  box-shadow: 
    0 0 40px rgba(59, 130, 246, 0.3),
    inset 0 0 20px rgba(255, 255, 255, 0.2);
  animation: dataFlow 5s infinite cubic-bezier(0.4, 0, 0.2, 1);
}

/* 动画2：图表增长效果 */
.animation-2 .animation-element {
  width: 70%;
  height: 70%;
  background: conic-gradient(from 0deg, #3b82f6 0%, #8b5cf6 25%, #6366f1 50%, transparent 50%);
  border-radius: 50%;
  box-shadow: 0 0 40px rgba(59, 130, 246, 0.3);
  animation: chartGrow 8s infinite cubic-bezier(0.4, 0, 0.2, 1);
}

/* 动画3：系统连接效果 */
.animation-3 .animation-element {
  width: 70%;
  height: 70%;
  background: repeating-linear-gradient(
    45deg,
    #3b82f6,
    #3b82f6 15px,
    #8b5cf6 15px,
    #8b5cf6 30px
  );
  clip-path: polygon(25% 0%, 75% 0%, 100% 25%, 100% 75%, 75% 100%, 25% 100%, 0% 75%, 0% 25%);
  box-shadow: 0 0 40px rgba(139, 92, 246, 0.3);
  animation: systemConnect 10s infinite cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes dataFlow {
  0% {
    transform: translateY(-50%) scaleY(0.5);
    opacity: 0.5;
  }
  50% {
    transform: translateY(0) scaleY(1);
    opacity: 1;
  }
  100% {
    transform: translateY(50%) scaleY(0.5);
    opacity: 0.5;
  }
}

@keyframes chartGrow {
  0% {
    transform: rotate(0deg) scale(0.8);
    opacity: 0.5;
  }
  50% {
    transform: rotate(180deg) scale(1);
    opacity: 1;
  }
  100% {
    transform: rotate(360deg) scale(0.8);
    opacity: 0.5;
  }
}

@keyframes systemConnect {
  0% {
    transform: rotate(0deg) scale(0.8);
    opacity: 0.5;
  }
  50% {
    transform: rotate(45deg) scale(1);
    opacity: 1;
  }
  100% {
    transform: rotate(0deg) scale(0.8);
    opacity: 0.5;
  }
}

@keyframes glowPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

@keyframes particleFloat {
  0%, 100% { transform: translate(0, 0); opacity: 0; }
  25% { transform: translate(100px, -50px); opacity: 1; }
  50% { transform: translate(200px, 0); opacity: 0.5; }
  75% { transform: translate(100px, 50px); opacity: 1; }
}

.feature-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  margin-bottom: 100px;
  align-items: center;
}

.feature-section:nth-child(even) {
  direction: rtl;
}

.feature-section:nth-child(even) .feature-details {
  direction: ltr;
}

.feature-badge {
  display: inline-block;
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(139, 92, 246, 0.1));
  color: #3b82f6;
  border-radius: 20px;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  border: 1px solid rgba(59, 130, 246, 0.2);
  padding: 8px 16px;
  font-weight: 600;
  position: relative;
  overflow: hidden;
}

.feature-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.6),
    transparent
  );
  transform: translateX(-100%);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  100% {
    transform: translateX(100%);
  }
}

.feature-details h2 {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: #1f2937;
  font-size: 2.5rem;
  background: linear-gradient(135deg, #1e40af, #3b82f6);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  letter-spacing: -1px;
}

.feature-details p {
  color: #6b7280;
  line-height: 1.7;
  margin-bottom: 2rem;
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-list li {
  margin: 1rem 0;
  padding-left: 2rem;
  position: relative;
  color: #4b5563;
}

.feature-list li::before {
  content: '→';
  position: absolute;
  left: 0;
  color: #3b82f6;
}

.feature-point {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(59, 130, 246, 0.05);
  border-radius: 12px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.feature-point:hover {
  transform: translateX(10px);
  background: rgba(59, 130, 246, 0.1);
}

.feature-point i {
  color: #3b82f6;
  font-size: 1.2rem;
}

@media (max-width: 768px) {
  .feature-section {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  
  .feature-section:nth-child(even) {
    direction: ltr;
  }

  .feature-animation {
    height: 300px;
  }

  .gradient-text {
    font-size: 2.5rem;
  }

  .header-subtitle {
    font-size: 1.2rem;
  }

  .feature-details h2 {
    font-size: 2rem;
  }
}

.comparison-section {
  margin-top: 120px;
  padding: 60px 20px;
  background: linear-gradient(180deg, #f8fafc, #fff);
  border-radius: 40px;
}

.comparison-title {
  font-size: 2.8rem;
  font-weight: 800;
  background: linear-gradient(135deg, #1e40af, #3b82f6);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 1rem;
  text-align: center;
}

.comparison-subtitle {
  text-align: center;
  color: #64748b;
  font-size: 1.2rem;
  margin-bottom: 3rem;
}

.comparison-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 40px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.comparison-card {
  background: white;
  border-radius: 24px;
  padding: 40px;
  position: relative;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
}

.comparison-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
}

.comparison-card.recommended {
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  border: none;
  box-shadow: 0 25px 50px -12px rgba(37, 99, 235, 0.35);
}

.comparison-card.recommended:hover {
  transform: translateY(-10px) scale(1.02);
}

.comparison-header {
  margin-bottom: 30px;
  position: relative;
}

.header-content h3 {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: #1e293b;
}

.recommended .header-content h3 {
  color: white;
}

.header-decoration {
  width: 40px;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, transparent);
  border-radius: 2px;
  margin-top: 10px;
}

.recommended .header-decoration {
  background: linear-gradient(90deg, white, rgba(255,255,255,0.3));
}

.recommended-badge {
  position: absolute;
  top: -15px;
  right: -15px;
  background: #22c55e;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.recommended-badge i {
  font-size: 0.8rem;
  color: #fbbf24;
}

.comparison-list {
  list-style: none;
  padding: 0;
}

.comparison-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin: 8px 0;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: rgba(241, 245, 249, 0.5);
}

.recommended .comparison-list li {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.comparison-list li:hover {
  transform: translateX(10px);
  background: rgba(241, 245, 249, 0.8);
}

.recommended .comparison-list li:hover {
  background: rgba(255, 255, 255, 0.2);
}

.feature-icon {
  font-size: 1.2rem;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comparison-list li:not(.highlight) .feature-icon {
  color: #94a3b8;
}

.comparison-list li.highlight .feature-icon {
  color: #22c55e;
}

.recommended .comparison-list li .feature-icon {
  color: white;
}

.point-text {
  font-size: 1rem;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .comparison-grid {
    grid-template-columns: 1fr;
  }
  
  .comparison-title {
    font-size: 2rem;
  }
  
  .comparison-subtitle {
    font-size: 1rem;
  }
  
  .comparison-card {
    padding: 30px;
  }
}

@keyframes titleFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes lineScan {
  0% { transform: translateX(-100%); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translateX(100%); opacity: 0; }
}

/* 添加滚动显示动画 */
[v-scroll-show] {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

[v-scroll-show].is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* 添加滤镜样式 */
.filters {
  position: absolute;
  width: 0;
  height: 0;
  pointer-events: none;
  opacity: 0;
}

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

@media (max-width: 768px) {
  .back-home {
    top: 10px;
    left: 10px;
  }

  .back-btn {
    padding: 8px 16px;
    font-size: 0.9rem;
  }
}

@media (max-width: 640px) {
  .page-header {
    height: 300px; /* 移动端高度调整 */
  }
}
</style>
