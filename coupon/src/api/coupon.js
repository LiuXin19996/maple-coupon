import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  withCredentials: true
})

// 请求拦截器
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  if (token) {
    config.headers['token'] = token
  }
  if (username) {
    config.headers['username'] = username
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    if (response.data && response.data.success) {
      return response.data
    }
    return Promise.reject(response.data || response)
  },
  async error => {
    const originalRequest = error.config
    if (error.response) {
      switch (error.response.status) {
        case 401:
          if (!originalRequest._retry) {
            originalRequest._retry = true
            try {
              const refreshToken = localStorage.getItem('refreshToken')
              if (refreshToken) {
                const { data } = await apiClient.post('/api/auth/refresh-token', { refreshToken })
                localStorage.setItem('token', data.token)
                localStorage.setItem('refreshToken', data.refreshToken)
                originalRequest.headers.Authorization = `Bearer ${data.token}`
                return apiClient(originalRequest)
              }
            } catch (refreshError) {
              localStorage.removeItem('token')
              localStorage.removeItem('refreshToken')
              localStorage.removeItem('username')
              if (window.location.pathname !== '/login') {
                window.$message.error('登录已过期，请重新登录')
                window.location.href = '/login'
              }
              return Promise.reject(refreshError)
            }
          }
          break
        case 403:
          error.message = '拒绝访问'
          break
        case 404:
          error.message = '请求地址出错'
          break
        case 500:
          error.message = '服务器内部错误'
          break
        default:
          error.message = `连接错误${error.response.status}`
      }
    } else if (error.request) {
      error.message = '服务器未响应'
    } else {
      error.message = '连接服务器失败'
    }
    return Promise.reject(error)
  }
)

export const couponAPI = {
  login: (credentials) => apiClient.post('/api/auth/user/login', credentials),
  register: (data) => apiClient.post('/api/auth/user/register', data),
  getCouponTemplate: (couponTemplateId) => 
    apiClient.get('/api/merchant-admin/coupon-template/find', {
      params: { couponTemplateId }
    }),
  findCouponTemplate: (queryParams) =>
    apiClient.get('/api/merchant-admin/coupon-template/find', {
      params: queryParams
    }),
  findEngineCouponTemplate: (queryParams) =>
    apiClient.get('/api/auth/engine/coupon-template/query', {
      params: queryParams
    }),
  getCouponTemplateRemindList: () => 
    apiClient.get('/api/engine/coupon-template-remind/list'),
  cancelCouponTemplateRemind: (data) =>
    apiClient.post('/api/engine/coupon-template-remind/cancel', data),
  refreshToken: (refreshToken) =>
    apiClient.post('/api/auth/refresh-token', { refreshToken }),
  increaseNumberCouponTemplate: (data) => {
    if (!data?.couponTemplateId || !data?.number) {
      return Promise.reject(new Error('必要参数缺失'));
    }
    return apiClient.post('/api/merchant-admin/coupon-template/increase-number', {
      couponTemplateId: data.couponTemplateId,
      number: Number(data.number)
    });
  },
  createCouponTemplateRemind: (data) => {
    if (!data?.couponTemplateId) {
      return Promise.reject(new Error('必要参数缺失'));
    }
    return apiClient.post('/api/engine/coupon-template-remind/create', {
      couponTemplateId: data.couponTemplateId,
      shopNumber: data.shopNumber,
      type: data.type,
      remindTime: data.remindTime
    });
  },
  redeemCoupon: (data) => {
    return apiClient.post('/api/engine/user-coupon/redeem', {
      source: data.source,
      shopNumber: data.shopNumber,
      couponTemplateId: data.couponTemplateId
    });
  },
  settlementCouponQuery: (data) => {
    return apiClient.post('/api/auth/settlement/coupon-query', {
      orderAmount: data.orderAmount,
      shopNumber: data.shopNumber,
      goodsList: data.goodsList
    });
  }
}

export default apiClient
