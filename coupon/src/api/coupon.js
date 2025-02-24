import axios from 'axios'
import { isNotEmpty } from '@/utils/plugins.js'
const VITE_API_BASE_URL = '/api/auth'
const apiClient = axios.create({
  baseURL: VITE_API_BASE_URL,
  timeout: 2000,
})
// 请求拦截器
apiClient.interceptors.request.use(config => {
  console.log('请求:', config)
  // 获取认证信息
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  config.headers.token = isNotEmpty(token) ? token : ''
  config.headers.username = isNotEmpty(username) ? username : ''
  config.headers.Authorization = `Bearer ${token}`
  return config
}, error => {
  return Promise.reject(error)
})
// 响应拦截器
apiClient.interceptors.response.use(
  (res) => {
    if (res.data.success) {
      // 请求成功对响应数据做处理，此处返回的数据是axios.then(res)中接收的数据
      // code值为 0 或 200 时视为成功
      return Promise.resolve(res)
    }
    return Promise.reject(res)
  },
  (err) => {
    // 在请求错误时要做的事儿
    // 此处返回的数据是axios.catch(err)中接收的数据
    console.log(err)
    if (err.data.status === 401) {
      // 清除本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      // 更新状态
      this.userStore.updateLoginStatus();
      router.push('/login')
    }
    return Promise.reject(err)
  }
)

export const couponAPI = {
  // 登录
  login: (credentials) => apiClient.post('user/login', credentials),
  // 注册
  register: (data) => apiClient.post('user/register', data),
  // 用户查询优惠券
  findEngineCouponTemplate: (queryParams) =>
    apiClient.get('engine/coupon-template/query', {
      params: queryParams
    }),
  // 用户查询优惠券兑换提醒
  getCouponTemplateRemindList: () =>
    apiClient.get('engine/coupon-template-remind/list'),
  // 用户取消优惠券兑换提醒
  cancelCouponTemplateRemind: (data) =>
    apiClient.post('engine/coupon-template-remind/cancel', data),
  // 增加发行量
  increaseNumberCouponTemplate: (data) => {
    if (!data?.couponTemplateId || !data?.number) {
      return Promise.reject(new Error('必要参数缺失'));
    }
    return apiClient.post('merchant-admin/coupon-template/increase-number', {
      couponTemplateId: data.couponTemplateId,
      number: Number(data.number)
    });
  },
  // 用户创建提醒
  createCouponTemplateRemind: (data) => {
    if (!data?.couponTemplateId) {
      return Promise.reject(new Error('必要参数缺失'));
    }
    return apiClient.post('engine/coupon-template-remind/create', {
      couponTemplateId: data.couponTemplateId,
      shopNumber: data.shopNumber,
      type: data.type,
      remindTime: data.remindTime
    });
  },
  // 用户兑换优惠券
  redeemCoupon: (data) => {
    return apiClient.post('engine/user-coupon/redeem', {
      source: data.source,
      shopNumber: data.shopNumber,
      couponTemplateId: data.couponTemplateId
    });
  },
  // 结算优惠券
  settlementCouponQuery: (data) => {
    return apiClient.post('settlement/coupon-query', {
      orderAmount: data.orderAmount,
      shopNumber: data.shopNumber,
      goodsList: data.goodsList
    });
  },
  // 创建优惠券
  createCouponTemplate: (data) => {
    return apiClient.post('merchant-admin/coupon-template/create', data);
  },
  // 获取优惠券列表
  getCouponTemplatePage: (params) =>
    apiClient.get('merchant-admin/coupon-template/page', { params }),

  // 终止优惠券
  terminateCouponTemplate: (couponTemplateId) =>
    apiClient.post('merchant-admin/coupon-template/terminate', { couponTemplateId }),

  // 删除优惠券
  deleteCouponTemplate: (couponTemplateId) =>
    apiClient.delete('merchant-admin/coupon-template/delete', {
      params: { couponTemplateId }
    }),

  // 获取优惠券详情
  getCouponTemplateDetail: (couponTemplateId) =>
    apiClient.get('merchant-admin/coupon-template/find', {
      params: { couponTemplateId }
    }),

  // 获取用户信息
  getUserInfo: (username) =>
    apiClient.get(`actual/user/${username}`),

  // 更新用户信息
  updateUserInfo: (data) => {
    const token = localStorage.getItem('token')
    return apiClient.put('user', data, {
      params: { token }
    })
  },

  // 用户登出
  logout: (username, token) =>
     apiClient.delete(`user/logout`, {
      params: { username, token }
    })
}

export default apiClient
