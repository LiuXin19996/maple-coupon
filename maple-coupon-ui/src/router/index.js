import { createRouter, createWebHistory } from 'vue-router'
import Home from '../components/Home.vue'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import Features from '../views/Features.vue'
import About from '../views/About.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/features',
    name: 'features',
    component: Features
  },
  {
    path: '/about',
    name: 'about',
    component: About
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/coupon-template',
    children: [
      {
        path: 'coupon-template',
        name: 'CouponTemplateManagement',
        component: () => import('../components/CouponTemplateManagement.vue'),
        meta: { requiresAuth: true, title: '优惠券管理', keepAlive: true }
      },
      {
        path: 'coupon-template/create',
        name: 'CouponTemplateCreate',
        component: () => import('../components/CouponTemplateCreate.vue'),
        meta: { requiresAuth: true, title: '创建优惠券', keepAlive: true }
      },
      {
        path: 'coupon-template/query',
        name: 'CouponTemplateRedeem',
        component: () => import('../components/CouponTemplateRedeem.vue'),
        meta: { requiresAuth: true, title: '优惠券兑换', keepAlive: true }
      },
      {
        path: 'coupon-template-detail/:id',
        name: 'CouponTemplateDetail',
        component: () => import('@/components/CouponTemplateDetail.vue'),
        meta: { requiresAuth: true, title: '优惠券详情', keepAlive: true }
      },
      {
        path: 'coupon-remind',
        name: 'CouponTemplateRemindList',
        component: () => import('@/components/CouponTemplateRemindList.vue'),
        meta: { requiresAuth: true, title: '优惠券提醒', keepAlive: true }
      },
      {
        path: 'user-profile',
        name: 'UserProfile',
        component: () => import('@/components/UserProfile.vue'),
        meta: { requiresAuth: true, title: '用户信息', keepAlive: true }
      },
      {
        path: 'settlement-coupon-query',
        name: 'SettlementCouponQuery',
        component: () => import('@/components/SettlementCouponQuery.vue'),
        meta: { requiresAuth: true, title: '优惠券结算', keepAlive: true
        }
      },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// Add authentication guard
router.beforeEach((to, from, next) => {
  // 添加错误处理
  try {
    const token = localStorage.getItem('token')
    const isLoginPage = to.path === '/login'
    const isRegisterPage = to.path === '/register'

    // 如果访问登录/注册页面且有token，跳转到首页
    if ((isLoginPage || isRegisterPage) && token) {
      next('/')
      return
    }

    // 处理需要认证的页面
    const authRequired = to.matched.some(record => record.meta.requiresAuth)
    if (authRequired && !token) {
      next('/login')
      return
    }

    // 处理详情页缺少参数的情况
    if (to.name === 'CouponTemplateDetail' && !to.params.id) {
      next('/admin/coupon-template')
      return
    }

    next()
  } catch (error) {
    console.error('Route error:', error)
    next(false)
  }
})

export default router
