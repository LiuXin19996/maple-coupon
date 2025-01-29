<template>
  <div class="about-page">
    <!-- 添加返回首页按钮 -->
    <div class="back-home">
      <router-link to="/" class="back-btn">
        <i class="fas fa-arrow-left"></i>
        返回首页
      </router-link>
    </div>

    <header class="page-header">
      <div class="header-content">
        <h1 class="gradient-text">关于 MapleCoupon</h1>
        <p class="header-subtitle">基于Spring Cloud的分布式优惠券系统</p>
        <div class="header-decoration">
          <div class="decoration-circle"></div>
        </div>
      </div>
      <div class="header-background">
        <div class="bg-pattern"></div>
      </div>
    </header>

    <div class="about-content">
      <!-- 项目简介 -->
      <section class="company-intro" v-scroll-show>
        <h2 class="section-title">
          <span class="title-decoration">——</span>
          项目简介
          <span class="title-decoration">——</span>
        </h2>
        <div class="intro-grid">
          <div class="intro-text">
            <div class="intro-header">
              <div class="tech-tag">Spring Cloud</div>
              <div class="tech-tag">分布式架构</div>
              <div class="tech-tag">高可用设计</div>
            </div>
            <p class="intro-description">
              <span class="highlight">MapleCoupon</span>是一个基于Spring Cloud微服务架构的分布式优惠券系统，
              采用前沿技术栈，实现了高性能、高可用的企业级解决方案。系统整合了
              <span class="tech-keyword">ShardingSphere</span>分库分表、
              <span class="tech-keyword">Redis</span>多级缓存、
              <span class="tech-keyword">RocketMQ</span>消息队列等中间件，
              能够支持<span class="emphasis">10万级</span>并发请求处理。
            </p>
            <div class="stats-container">
              <div class="company-stats">
                <div class="stat-item" v-for="(stat, index) in companyStats" :key="index"
                  :style="{ '--delay': `${index * 0.1}s` }">
                  <div class="stat-icon">{{ stat.icon }}</div>
                  <h3 class="stat-value">{{ stat.value }}</h3>
                  <p class="stat-label">{{ stat.label }}</p>
                </div>
              </div>
            </div>
            <div class="company-values">
              <h3 class="values-title">我们的价值观</h3>
              <div class="values-grid">
                <div v-for="(value, index) in companyValues" :key="index" class="value-card"
                  :style="{ '--delay': `${index * 0.1}s` }">
                  <div class="value-icon-wrapper">
                    <span class="value-icon">{{ value.icon }}</span>
                  </div>
                  <div class="value-content">
                    <h4>{{ value.title }}</h4>
                    <p>{{ value.description }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="intro-visual">
            <div class="tech-diagram">
              <div class="diagram-container">
                <!-- 主要圆环 -->
                <div class="diagram-ring"></div>
                <!-- 动态连接线 -->
                <div class="diagram-lines">
                  <div class="line line-1"></div>
                  <div class="line line-2"></div>
                  <div class="line line-3"></div>
                </div>
                <!-- 技术节点 -->
                <div class="tech-nodes">
                  <div class="node node-1">
                    <i class="fas fa-cloud"></i>
                    <span>Spring Cloud</span>
                  </div>
                  <div class="node node-2">
                    <i class="fas fa-database"></i>
                    <span>Redis</span>
                  </div>
                  <div class="node node-3">
                    <i class="fas fa-server"></i>
                    <span>ShardingSphere</span>
                  </div>
                  <div class="node node-4">
                    <i class="fas fa-rocket"></i>
                    <span>RocketMQ</span>
                  </div>
                </div>
                <!-- 中心圆点 -->
                <div class="center-point">
                  <div class="pulse-ring"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 发展历程 -->
      <section class="timeline-section" v-scroll-show>
        <h2 class="section-title">
          <span class="title-decoration">——</span>
          发展历程
          <span class="title-decoration">——</span>
        </h2>
        <div class="timeline">
          <div class="timeline-progress"></div>
          <div v-for="(milestone, index) in milestones" :key="index" class="timeline-item"
            :class="{ 'right': index % 2 !== 0 }" :style="{ '--delay': `${index * 0.2}s` }">
            <div class="timeline-content">
              <div class="milestone-marker">
                <div class="marker-dot"></div>
                <div class="marker-line"></div>
              </div>
              <div class="milestone-card">
                <span class="timeline-date">{{ milestone.date }}</span>
                <h3 class="milestone-title">{{ milestone.title }}</h3>
                <p class="milestone-description">{{ milestone.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 团队介绍 -->
      <section class="team-section" v-scroll-show>
        <h2 class="section-title">
          <span class="title-decoration">——</span>
          核心团队
          <span class="title-decoration">——</span>
        </h2>
        <div class="team-grid">
          <div v-for="member in teamMembers" :key="member.id" class="team-member"
            :style="{ '--delay': `${member.id * 0.1}s` }">
            <div class="member-card">
              <div class="member-cover">
                <div class="member-avatar">
                  <div class="avatar-circle">
                    <img :src="member.avatar" :alt="member.name" style="width: 100%; height: 100%; object-fit: cover;">
                  </div>
                  <div class="avatar-glow"></div>
                </div>
              </div>
              <div class="member-content">
                <h3 class="member-name">{{ member.name }}</h3>
                <p class="member-title">{{ member.title }}</p>
                <div class="member-divider"></div>
                <p class="member-bio">{{ member.bio }}</p>
                <div class="member-social">
                  <a v-for="social in member.social" :key="social.platform" :href="social.link" class="social-link"
                    :class="social.platform" target="_blank">
                    <i :class="social.icon"></i>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 技术支持部分 -->
      <section class="contact-section" v-scroll-show>
        <div class="section-background">
          <div class="bg-pattern"></div>
          <div class="floating-circles">
            <div class="circle circle-1"></div>
            <div class="circle circle-2"></div>
            <div class="circle circle-3"></div>
          </div>
        </div>
        <h2 class="section-title">
          <span class="title-decoration">——</span>
          技术支持
          <span class="title-decoration">——</span>
        </h2>
        <div class="contact-container">
          <div class="contact-cards-wrapper">
            <!-- 开源地址卡片 -->
            <div class="contact-card" data-aos="fade-up" data-aos-delay="100">
              <div class="card-background">
                <div class="gradient-overlay"></div>
              </div>
              <div class="card-icon">
                <div class="icon-wrapper">
                  <i class="fas fa-code-branch"></i>
                  <div class="icon-glow"></div>
                  <div class="icon-pulse"></div>
                </div>
              </div>
              <div class="card-content">
                <h3>博客</h3>
                <a href="https://github.com/maplecoupon" target="_blank" class="link-hover">
                  <i class="fab fa-github"></i>
                  <span class="link-text">blog.fxink.cn</span>
                  <i class="fas fa-external-link-alt"></i>
                </a>
              </div>
            </div>

            <!-- 技术交流卡片 -->
            <div class="contact-card" data-aos="fade-up" data-aos-delay="200">
              <div class="card-background">
                <div class="gradient-overlay"></div>
              </div>
              <div class="card-icon">
                <div class="icon-wrapper">
                  <i class="fas fa-comments"></i>
                  <div class="icon-glow"></div>
                  <div class="icon-pulse"></div>
                </div>
              </div>
              <div class="card-content">
                <h3>技术交流</h3>
                <a href="mailto:fx20031215@163.com" class="link-hover">
                  <i class="fas fa-envelope"></i>
                  <span class="link-text">fx20031215@163.com</span>
                  <i class="fas fa-arrow-right"></i>
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const companyStats = [
  { value: '10万+', label: '并发处理能力', icon: '⚡' },
  { value: '99.99%', label: '系统可用性', icon: '🛡️' },
  { value: '50ms', label: '平均响应时间', icon: '⏱️' },
  { value: '300+', label: '接口调用量(万/日)', icon: '📊' }
]

const companyValues = [
  {
    icon: '🎯',
    title: '技术创新',
    description: '采用Spring Cloud微服务架构，引入前沿技术解决方案'
  },
  {
    icon: '🔒',
    title: '高可用设计',
    description: '分布式架构设计，确保系统稳定性和可扩展性'
  },
  {
    icon: '📈',
    title: '性能至上',
    description: '多级缓存策略，消息队列削峰，优化系统性能'
  }
]

const milestones = [
  {
    date: '2024年10月',
    title: '系统架构搭建',
    description: '使用Spring Boot 3.0，引入技术前沿特性'
  },
  {
    date: '2024年11月',
    title: '性能优化',
    description: '引入ShardingSphere分库分表、RocketMQ等方案，系统性能提升30%'
  },
  {
    date: '2024年12月',
    title: '微服务改造',
    description: '完成系统微服务化改造，采用Spring Cloud架构'
  },
  {
    date: '2024年1月',
    title: '项目发布',
    description: '上线分布式优惠券系统'
  }
]

const teamMembers = [
  {
    id: 1,
    name: 'maple',
    title: '研发工程师',
    avatar: '/avatar.jpg',
    bio: '一年Java经验，熟悉Java基础和主流框架应用，负责系统架构和优化',
    social: [
      { platform: 'github', icon: 'fab fa-github', link: 'https://github.com/mapleplus' },
    ]
  }
]
</script>

<style scoped>
.about-page {
  padding-top: 0;
  background: linear-gradient(to bottom, #f8fafc, #ffffff);
  color: #1f2937;
}

.page-header {
  position: relative;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
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

.text-wrapper {
  position: relative;
  display: inline-block;
}

.letters {
  display: inline-block;
}

.line {
  position: absolute;
  left: 0;
  bottom: -10px;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  transform-origin: left;
  animation: lineExtend 1s ease-in-out forwards;
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

.header-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.bg-pattern {
  width: 100%;
  height: 100%;
  opacity: 0.1;
  background-image: radial-gradient(#3b82f6 1px, transparent 1px);
  background-size: 20px 20px;
}

@keyframes lineExtend {
  0% { transform: scaleX(0); }
  100% { transform: scaleX(1); }
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

.about-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 37px 24px;
}

section {
  margin-bottom: 120px;
}

h2 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 3rem;
  text-align: center;
  position: relative;
}

h2::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #4f46e5, #3b82f6);
  border-radius: 2px;
}

.intro-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 60px;
  background: white;
  padding: 40px;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.06);
}

.intro-header {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tech-tag {
  padding: 6px 16px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  color: white;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
}

.intro-description {
  font-size: 1.1rem;
  line-height: 1.8;
  color: #4b5563;
  margin-bottom: 40px;
}

.highlight {
  color: #4f46e5;
  font-weight: 600;
}

.tech-keyword {
  color: #4338ca;
  font-weight: 500;
  padding: 2px 8px;
  background: rgba(79, 70, 229, 0.1);
  border-radius: 4px;
}

.emphasis {
  font-weight: 700;
  color: #4f46e5;
}

.stats-container {
  position: relative;
  margin: 60px 0;
}

.company-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.stat-item {
  background: white;
  padding: 32px 24px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(79, 70, 229, 0.1);
  position: relative;
  overflow: hidden;
}

.stat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #4f46e5, #4338ca);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s ease;
}

.stat-item:hover::before {
  transform: scaleX(1);
}

.stat-icon {
  font-size: 2.5rem;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: #4f46e5;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  color: #6b7280;
  font-size: 0.95rem;
}

.values-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 2rem;
  color: #1f2937;
  text-align: center;
}

.values-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.value-card {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  border: 1px solid rgba(79, 70, 229, 0.1);
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.value-icon-wrapper {
  padding: 16px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  border-radius: 12px;
  font-size: 1.5rem;
  color: white;
}

.value-content h4 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.value-content p {
  color: #6b7280;
  line-height: 1.6;
  font-size: 0.95rem;
}

.intro-visual {
  position: relative;
  height: 100%;
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tech-diagram {
  width: 100%;
  height: 100%;
  position: relative;
}

.diagram-container {
  position: relative;
  width: 500px;
  height: 500px;
  margin: auto;
}

.diagram-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 400px;
  height: 400px;
  border: 4px solid rgba(79, 70, 229, 0.1);
  border-radius: 50%;
  box-shadow: 0 0 50px rgba(79, 70, 229, 0.1);
  animation: rotateRing 20s linear infinite;
}

.diagram-ring::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  background: linear-gradient(45deg, #4f46e5, transparent, #4338ca);
  animation: borderGlow 4s linear infinite;
}

.diagram-lines .line {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 180px;
  height: 2px;
  background: linear-gradient(90deg, rgba(79, 70, 229, 0.1), rgba(79, 70, 229, 0.6));
  transform-origin: left;
  filter: drop-shadow(0 0 8px rgba(79, 70, 229, 0.3));
}

.line-1 { transform: rotate(0deg); }
.line-2 { transform: rotate(120deg); }
.line-3 { transform: rotate(240deg); }

.tech-nodes .node {
  position: absolute;
  width: 100px;
  height: 100px;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(79, 70, 229, 0.15);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  cursor: pointer;
}

.node-1 { top: 15%; left: 50%; }
.node-2 { top: 50%; left: 85%; }
.node-3 { top: 85%; left: 50%; }
.node-4 { top: 50%; left: 15%; }

.node:hover {
  transform: translate(-50%, -50%) scale(1.1);
  box-shadow: 0 12px 40px rgba(79, 70, 229, 0.25);
}

.node i {
  font-size: 2rem;
  color: #4f46e5;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.node span {
  font-size: 0.9rem;
  color: #4f46e5;
  font-weight: 500;
  text-align: center;
}

.center-point {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  border-radius: 50%;
  box-shadow: 0 0 30px rgba(79, 70, 229, 0.4);
}

.pulse-ring {
  position: absolute;
  inset: -10px;
  border: 2px solid #4f46e5;
  border-radius: 50%;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes rotateRing {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes borderGlow {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.7; }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

@media (max-width: 768px) {
  .diagram-container {
    width: 300px;
    height: 300px;
  }

  .diagram-ring {
    width: 250px;
    height: 250px;
  }

  .diagram-lines .line {
    width: 120px;
  }

  .tech-nodes .node {
    width: 80px;
    height: 80px;
  }

  .node i {
    font-size: 1.5rem;
  }

  .node span {
    font-size: 0.8rem;
  }
}

.timeline-section {
  position: relative;
  padding: 100px 0;
  background: linear-gradient(to bottom, #ffffff, #f8fafc);
}

.timeline {
  position: relative;
  max-width: 1200px;
  margin: 60px auto 0;
  padding: 40px 20px;
}

.timeline-progress {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 100%;
  background: linear-gradient(to bottom, #4f46e5 50%, #e5e7eb 50%);
  background-size: 100% 40px;
  animation: progressGrow 2s linear infinite;
  border-radius: 2px;
}

.timeline-item {
  width: calc(50% - 50px);
  margin-bottom: 80px;
  position: relative;
  opacity: 0;
  transform: translateY(30px);
  animation: fadeInUp 0.8s ease forwards;
  animation-delay: var(--delay);
}

.timeline-item.right {
  margin-left: calc(50% + 50px);
}

.milestone-marker {
  position: absolute;
  top: 50%;
  width: 40px;
  height: 2px;
}

.timeline-item:not(.right) .milestone-marker {
  right: -90px;
}

.timeline-item.right .milestone-marker {
  left: -90px;
}

.marker-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  background: #4f46e5;
  border-radius: 50%;
  box-shadow: 0 0 0 6px rgba(79, 70, 229, 0.1);
  animation: pulseDot 2s infinite;
}

.marker-line {
  width: 100%;
  height: 100%;
  background: #4f46e5;
}

.milestone-card {
  background: white;
  padding: 32px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(79, 70, 229, 0.1);
  transition: all 0.3s ease;
}

.milestone-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(79, 70, 229, 0.1);
  border-color: rgba(79, 70, 229, 0.2);
}

.timeline-date {
  display: inline-block;
  padding: 6px 16px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  color: white;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  margin-bottom: 16px;
}

.milestone-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  background: linear-gradient(to right, #4f46e5, #4338ca);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.milestone-description {
  color: #6b7280;
  line-height: 1.6;
  font-size: 1rem;
}

@keyframes progressGrow {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 0 80px;
  }
}

@keyframes pulseDot {
  0% {
    box-shadow: 0 0 0 0 rgba(79, 70, 229, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(79, 70, 229, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(79, 70, 229, 0);
  }
}

@media (max-width: 768px) {
  .timeline-progress {
    left: 30px;
  }

  .timeline-item,
  .timeline-item.right {
    width: calc(100% - 60px);
    margin-left: 60px;
  }

  .milestone-marker {
    left: -60px !important;
    width: 30px;
  }
}

.team-section {
  background: linear-gradient(to bottom, #ffffff, #f8fafc);
  padding: 120px 0;
  position: relative;
  overflow: hidden;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 40px;
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.team-member {
  opacity: 0;
  transform: translateY(30px);
  animation: fadeInUp 0.8s ease forwards;
  animation-delay: var(--delay);
}

.member-card {
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.member-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 30px 60px rgba(79, 70, 229, 0.1);
}

.member-cover {
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  height: 160px;  
  position: relative;
  overflow: visible;  
}

.member-cover::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23ffffff' fill-opacity='0.1' fill-rule='evenodd'/%3E%3C/svg%3E");
}

.member-avatar {
  width: 120px;
  height: 120px;
  position: absolute;
  left: 50%;
  top: 100%; 
  transform: translateX(-50%) translateY(-50%);
  border-radius: 50%;
  overflow: visible; 
}

.avatar-circle {
  width: 100%;
  height: 100%;
  background: #f3f4f6;
  border: 4px solid white;
  border-radius: 50%;
  position: relative;
  z-index: 1;
  overflow: hidden; 
}

.avatar-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;  
  display: block;  
}

.avatar-glow {
  position: absolute;
  inset: -5px;
  background: linear-gradient(45deg, #4f46e5, #4338ca);
  border-radius: 50%;
  z-index: 0;
  animation: glowPulse 2s ease-in-out infinite;
}

.member-content {
  padding: 90px 30px 30px;  
  text-align: center;
}

.member-name {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.member-title {
  color: #4f46e5;
  font-weight: 500;
  font-size: 1.1rem;
  margin-bottom: 16px;
}

.member-divider {
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #4f46e5, #4338ca);
  margin: 20px auto;
  border-radius: 2px;
}

.member-bio {
  color: #6b7280;
  line-height: 1.6;
  font-size: 0.95rem;
  margin-bottom: 24px;
}

.member-social {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-link {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #f3f4f6;
  color: #4f46e5;
  transition: all 0.3s ease;
}

.social-link:hover {
  background: #4f46e5;
  color: white;
  transform: translateY(-3px);
}

.social-link.github:hover {
  background: #333;
}

.social-link.linkedin:hover {
  background: #0077b5;
}

@keyframes glowPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.4;
  }
}

@media (max-width: 768px) {
  .team-grid {
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 30px;
    padding: 20px;
  }
  
  .member-content {
    padding: 60px 20px 25px;
  }
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1024px) {
  .header-content h1 {
    font-size: 3rem;
  }

  .intro-grid {
    grid-template-columns: 1fr;
    padding: 40px;
    gap: 60px;
  }

  .timeline::before {
    left: 30px;
  }

  .timeline-item {
    width: calc(100% - 60px);
    margin-left: 60px !important;
  }
}

@media (max-width: 640px) {
  .page-header {
    height: 350px;
  }

  .header-content h1 {
    font-size: 2.5rem;
  }

  .header-subtitle {
    font-size: 1.25rem;
  }

  .about-content {
    padding: 60px 20px;
  }

  section {
    margin-bottom: 80px;
  }

  h2 {
    font-size: 2rem;
  }

  .intro-grid {
    padding: 30px;
  }

  .company-stats {
    grid-template-columns: 1fr;
  }
}

.contact-section {
  background: linear-gradient(135deg, #f8fafc, #ffffff);
  padding: 120px 0;
  position: relative;
  overflow: hidden;
}

.section-background {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-pattern {
  width: 100%;
  height: 100%;
  opacity: 0.1;
  background-image: radial-gradient(#4f46e5 1px, transparent 1px);
  background-size: 30px 30px;
}

.floating-circles {
  position: absolute;
  inset: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
}

.circle-1 {
  top: 20%;
  left: 10%;
  width: 200px;
  height: 200px;
  background: rgba(79, 70, 229, 0.1);
  animation: floatCircle 20s linear infinite;
}

.circle-2 {
  top: 50%;
  right: 15%;
  width: 300px;
  height: 300px;
  background: rgba(99, 102, 241, 0.1);
  animation: floatCircle 25s linear infinite reverse;
}

.circle-3 {
  bottom: 10%;
  left: 35%;
  width: 150px;
  height: 150px;
  background: rgba(67, 56, 202, 0.1);
  animation: floatCircle 15s linear infinite;
}

@keyframes floatCircle {
  0% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(30px, 30px) rotate(180deg); }
  100% { transform: translate(0, 0) rotate(360deg); }
}

.contact-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.contact-cards-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-top: 40px;
}

.contact-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(79, 70, 229, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.card-icon {
  margin-bottom: 24px;
}

.card-content {
  width: 100%;
}

.card-content h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.link-hover {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(79, 70, 229, 0.05);
  border-radius: 12px;
  color: #4f46e5;
  text-decoration: none;
  transition: all 0.3s ease;
}

.link-text {
  flex: 1;
  text-align: left;
  font-size: 0.95rem;
}

.link-hover:hover {
  background: rgba(79, 70, 229, 0.1);
  transform: translateX(5px);
}

.icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: white;
  position: relative;
  transition: all 0.3s ease;
}

.icon-wrapper:hover {
  transform: scale(1.1) rotate(5deg);
}

@media (max-width: 768px) {
  .contact-cards-wrapper {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .contact-card {
    padding: 30px 20px;
  }

  .icon-wrapper {
    width: 60px;
    height: 60px;
    font-size: 1.5rem;
  }

  .card-content h3 {
    font-size: 1.25rem;
  }
}

@keyframes floatCircle {
  0% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(30px, 30px) rotate(180deg); }
  100% { transform: translate(0, 0) rotate(360deg); }
}

.contact-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 
    0 10px 30px rgba(0, 0, 0, 0.05),
    0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(79, 70, 229, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.card-background {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    45deg,
    rgba(79, 70, 229, 0.03),
    rgba(99, 102, 241, 0.03),
    rgba(67, 56, 202, 0.03)
  );
  opacity: 0;
  transition: opacity 0.3s ease;
}

.contact-card:hover {
  transform: translateY(-5px);
  box-shadow: 
    0 20px 40px rgba(79, 70, 229, 0.1),
    0 1px 5px rgba(79, 70, 229, 0.1);
}

.contact-card:hover .gradient-overlay {
  opacity: 1;
}

.icon-wrapper {
  position: relative;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  color: white;
  font-size: 1.5rem;
  transition: transform 0.3s ease;
}

.icon-pulse {
  position: absolute;
  inset: -2px;
  border: 2px solid #4f46e5;
  border-radius: 16px;
  animation: iconPulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes iconPulse {
  0% { transform: scale(1); opacity: 0.5; }
  100% { transform: scale(1.2); opacity: 0; }
}

.contact-item:hover .icon-wrapper {
  transform: scale(1.1) rotate(5deg);
}

.link-hover {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(79, 70, 229, 0.05);
  border-radius: 12px;
  color: #4f46e5;
  text-decoration: none;
  transition: all 0.3s ease;
}

.link-text {
  flex: 1;
}

.link-hover:hover {
  background: rgba(79, 70, 229, 0.1);
  transform: translateX(5px);
}

.contact-divider {
  position: relative;
  height: 1px;
  background: linear-gradient(
    to right,
    transparent,
    rgba(79, 70, 229, 0.1),
    transparent
  );
  margin: 30px 0;
  overflow: hidden;
}

.divider-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(79, 70, 229, 0.3),
    transparent
  );
  animation: dividerGlow 3s linear infinite;
}

@keyframes dividerGlow {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
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
</style>
