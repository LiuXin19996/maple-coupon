<template>
  <el-container class="admin-container">
    <!-- 顶部导航栏 -->
    <el-header class="admin-header">
      <div class="header-content">
        <div class="header-left">
          <!-- 添加 logo 区域 -->
          <div class="logo-container">
            <img src="/logo.png" alt="logo" class="logo-image hover-scale">
            <span class="logo-text">Maple-Coupon</span>
          </div>
          <div class="divider"></div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }" class="breadcrumb-item">首页</el-breadcrumb-item>
            <el-breadcrumb-item class="breadcrumb-item">{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="user-profile">
            <el-avatar :size="32" :src="userAvatar" class="user-avatar"></el-avatar>
            <span class="username">{{ username }}</span>
          </div>
        </div>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside class="admin-sidebar" width="200px">
        <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9"
          active-text-color="#409EFF">
          <el-menu-item index="/admin/coupon-template">
            <el-icon>
              <Collection />
            </el-icon>
            <span>优惠券管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/coupon-remind">
            <el-icon>
              <Bell />
            </el-icon>
            <span>优惠券预约提醒</span>
          </el-menu-item>
          <el-menu-item index="/admin/coupon-template/query">
            <el-icon>
              <Search />
            </el-icon>
            <span>优惠券兑换预约</span>
          </el-menu-item>
          <el-menu-item index="/admin/settlement-coupon-query">
            <el-icon>
              <Bell />
            </el-icon>
            <span>优惠券结算服务</span>
          </el-menu-item>
          <el-menu-item index="/admin/user-profile">
            <el-icon>
              <User />
            </el-icon>
            <span>个人信息</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="content-area">
        <div class="card-container">
          <router-view v-slot="{ Component }">
            <keep-alive>
              <component :is="Component" :key="$route.fullPath" />
            </keep-alive>
          </router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { Collection, CirclePlus, Search, Bell, User } from '@element-plus/icons-vue'

export default {
  components: {
    Collection,
    CirclePlus,
    Search,
    Bell,
    User
  },
  data() {
    return {
      userAvatar: '/avatar.png', 
      username: localStorage.getItem('username') || '淘风'
    }
  },
  computed: {
    activeMenu() {
      return this.$route.path
    },
    currentRouteName() {
      const routeMap = {
        'UserProfile': '个人信息',
        'CouponTemplateManagement': '优惠券管理',
        'CouponTemplateQuery': '优惠券兑换预约',
        'CouponTemplateRemindList': '优惠券预约提醒',
        'SettlementCouponQuery': '优惠券结算服务'
      }
      return routeMap[this.$route.name] || this.$route.meta?.title || ''
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.admin-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  position: relative;
  overflow: hidden;
}

.admin-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/pattern.png') repeat;
  opacity: 0.02;
  pointer-events: none;
}

/* 重置顶栏相关样式 */
.admin-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  height: 64px !important;
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 0;
}

.header-content {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 20px;
  padding-left: 8px;
  /* 修改这里，减少左侧padding */
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08) 0%, rgba(54, 207, 201, 0.08) 100%);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  width: auto;
  /* 移除固定宽度 */
  margin-left: 8px;
  /* 添加这行，增加左边距 */
  margin-right: 12px;
  white-space: nowrap;
  /* 防止文字换行 */
}

.header-right {
  display: flex;
  align-items: center;
  height: 100%;
}

.user-profile {
  display: flex;
  align-items: center;
  padding: 6px 18px;
  border-radius: 30px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 249, 250, 0.95) 100%);
  border: 1px solid rgba(64, 158, 255, 0.1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
}

.el-breadcrumb {
  line-height: normal;
  padding: 0;
}

.main-container {
  padding-top: 64px;
  height: calc(100vh - 64px);
}

.admin-sidebar {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  height: 100%;
  box-shadow: 4px 0 25px rgba(0, 0, 0, 0.15);
  position: fixed;
  top: 64px;
  left: 0;
  bottom: 0;
  z-index: 2;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-right: 1px solid rgba(255, 255, 255, 0.03);
  box-shadow:
    4px 0 25px rgba(0, 0, 0, 0.1),
    2px 0 8px rgba(0, 0, 0, 0.06);
}

.el-menu {
  border-right: none !important;
  padding-top: 12px;
  padding: 16px 8px;
  background: transparent !important;
}

.content-area {
  margin-left: 200px;
  padding: 24px;
  height: calc(100vh - 64px);
  overflow-y: auto;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out;
}

.card-container {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 24px;
  min-height: calc(100vh - 112px);
  overflow: visible;
  position: relative;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.04),
    0 1px 2px rgba(0, 0, 0, 0.02);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.card-container:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  transform: translateY(-4px);
  box-shadow:
    0 8px 30px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.03);
}

.card-container::before {
  content: '';
  position: absolute;
  top: -1px;
  left: -1px;
  right: -1px;
  bottom: -1px;
  background: linear-gradient(135deg, #60a5fa05 0%, #3b82f610 100%);
  border-radius: inherit;
  z-index: -1;
  transition: opacity 0.3s ease;
  opacity: 0;
}

.card-container:hover::before {
  opacity: 1;
}

/* 其他样式保持不变 */
.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.el-dropdown-link:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.username {
  margin-left: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.el-menu-item {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0 20px !important;
  margin: 4px 16px;
  height: 50px;
  line-height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  height: 56px !important;
  line-height: 56px !important;
  border-radius: 12px;
  margin: 8px 0;
  padding: 0 16px !important;
  position: relative;
  overflow: hidden;
}

.el-menu-item:hover {
  background: rgba(255, 255, 255, 0.08) !important;
  transform: translateX(4px);
  color: #ffffff !important;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%) !important;
  transform: translateX(6px);
  padding-left: 22px !important;
}

.el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.25) 0%, rgba(64, 158, 255, 0.15) 100%) !important;
  color: #ffffff !important;
  font-weight: 600;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%) !important;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.35);
  transform: translateX(6px);
  margin: 12px 0;
}

.el-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 60%;
  background: #409EFF;
  border-radius: 0 4px 4px 0;
  width: 4px;
  height: 24px;
  background: #60a5fa;
  border-radius: 0 4px 4px 0;
  box-shadow: 0 0 10px rgba(96, 165, 250, 0.5);
}

.el-menu-item i {
  margin-right: 12px;
  font-size: 18px;
  width: 24px;
  text-align: center;
  transition: all 0.3s ease;
  color: inherit;
  font-size: 20px;
}

.el-menu-item span {
  font-size: 14px;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
  font-size: 15px;
  font-weight: 500;
}

.el-menu-item::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #60a5fa, transparent);
  transition: width 0.3s ease;
  transform: translateY(-50%);
}

.el-menu-item:hover::after {
  width: 100%;
}

.el-breadcrumb {
  font-size: 14px;
  color: #606266;
  padding-left: 24px;
  line-height: 64px;
}

.el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #409EFF;
  font-weight: 600;
  background: linear-gradient(120deg, #409EFF, #36cfc9);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.router-view-enter-active {
  animation: fadeIn 0.3s ease-out;
}

/* 添加响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 0;
    transform: translateX(-100%);
  }

  .content-area {
    margin-left: 0;
    width: 100%;
  }
}

.admin-sidebar {
  transition: width 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 20px;
  padding-left: 8px;
  /* 修改这里，减少左侧padding */
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  padding: 0 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  border-radius: 12px;
  margin-right: 8px;
  padding: 8px 16px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08) 0%, rgba(54, 207, 201, 0.08) 100%);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  width: auto;
  /* 移除固定宽度 */
  margin-left: -29px;
  /* 添加这行，增加左边距 */
  margin-right: -100px;
  white-space: nowrap;
  /* 防止文字换行 */
}

.logo-container:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  border-color: rgba(64, 158, 255, 0.2);
}

.logo-image {
  height: 36px;
  width: 36px;
  object-fit: contain;
  transition: transform 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  height: 32px;
  width: 32px;
  height: 28px;
  width: 28px;
  object-fit: contain;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.logo-image.hover-scale:hover {
  transform: scale(1.12) rotate(5deg);
  filter: drop-shadow(0 4px 8px rgba(64, 158, 255, 0.3));
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(120deg, #409EFF 0%, #36cfc9 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  font-size: 22px;
  background: linear-gradient(120deg, #2563eb 0%, #0ea5e9 100%);
  letter-spacing: 0.5px;
  margin-left: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #2563eb;
  background: linear-gradient(120deg, #2563eb, #0ea5e9);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 0.5px;
  margin-left: -9px;
  white-space: nowrap;
}

.divider {
  width: 1px;
  height: 24px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.06) 0%, rgba(0, 0, 0, 0.12) 100%);
  margin: 0;
  opacity: 0.8;
  height: 28px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.04) 0%, rgba(0, 0, 0, 0.08) 100%);
  margin: 0 16px;
  height: 24px;
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
  margin: 0 20px;
}

.header-right {
  padding-right: 24px;
  height: 100%;
  display: flex;
  align-items: center;
}

.breadcrumb-item {
  transition: all 0.3s ease;
  position: relative;
}

.breadcrumb-item:hover .el-breadcrumb__inner {
  color: #409EFF !important;
  transform: translateX(3px);
  text-shadow: 0 2px 4px rgba(64, 158, 255, 0.1);
  transform: translateX(4px);
  background: linear-gradient(120deg, #2563eb 0%, #0ea5e9 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.6);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.8) 100%);
  padding: 6px 16px;
  border-radius: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.8);
  padding: 6px 18px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 249, 250, 0.95) 100%);
  border: 1px solid rgba(64, 158, 255, 0.1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03),
    0 0 0 1px rgba(64, 158, 255, 0.05);
}

.user-profile:hover {
  background: rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.15);
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08) 0%, rgba(54, 207, 201, 0.08) 100%);
  border-color: rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.12),
    0 0 0 1px rgba(64, 158, 255, 0.1);
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  border: 2px solid #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid #2563eb;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.user-profile:hover .user-avatar {
  border-color: #409EFF;
  transform: scale(1.05);
  transform: scale(1.08) rotate(5deg);
  border-color: #0ea5e9;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin: 0 4px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(120deg, #2563eb 0%, #0ea5e9 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-left: 12px;
  letter-spacing: 0.3px;
}

/* 添加滚动条样式 */
.el-menu::-webkit-scrollbar {
  width: 4px;
}

.el-menu::-webkit-scrollbar-track {
  background: transparent;
}

.el-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.el-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}

.el-menu-item:hover i {
  transform: scale(1.1) translateX(2px);
  color: #60a5fa !important;
}

.el-menu-item.is-active i {
  transform: scale(1.2);
  color: white !important;
}

.el-menu-item:hover span {
  color: #60a5fa !important;
}

.el-menu-item.is-active span {
  color: white !important;
  font-weight: 600;
  letter-spacing: 1px;
}

@media (max-width: 768px) {
  .logo-text {
    display: none;
  }

  .logo-container {
    padding: 6px;
    background: none;
    border: none;
    box-shadow: none;
  }

  .divider {
    margin: 0 12px;
  }

  .header-content {
    padding: 0 12px;
  }

  .fade-transform-enter-from {
    opacity: 0;
    transform: translateX(30px);
  }

  .fade-transform-leave-to {
    opacity: 0;
    transform: translateX(-30px);
  }

  /* 美化滚动条 */
  ::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  ::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.02);
    border-radius: 3px;
  }

  ::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
    transition: all 0.3s ease;
  }

  ::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.2);
  }

  /* 添加内容区域的动画效果 */
  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }

    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

/* 更新面包屑导航样式 */
.el-breadcrumb {
  font-size: 15px;
  padding-left: 15px;
  line-height: 64px;
}

.el-breadcrumb__item {
  display: flex;
  align-items: center;
}

/* 首页样式 */
.el-breadcrumb__item:first-child .el-breadcrumb__inner {
  color: #64748b !important;
  font-weight: 500;
  transition: all 0.3s ease;
}

.el-breadcrumb__item:first-child .el-breadcrumb__inner:hover {
  color: #2563eb !important;
  transform: translateX(2px);
}

/* 当前页面样式 */
.el-breadcrumb__item:last-child .el-breadcrumb__inner {
  font-weight: 600;
  font-size: 16px;
  background: linear-gradient(120deg, #2563eb, #0ea5e9);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(37, 99, 235, 0.1);
}

/* 分隔符样式 */
.el-breadcrumb__separator {
  color: #94a3b8;
  margin: 0 12px;
  font-weight: 600;
}

/* 面包屑项目悬停效果 */
.el-breadcrumb__inner {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-breadcrumb__inner:hover {
  opacity: 0.8;
  transform: translateX(2px);
}
</style>
