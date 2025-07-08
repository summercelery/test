import api from './index'

// 用户相关API
export const userApi = {
  // 获取用户信息
  getProfile: () => api.get('/user/profile'),
  
  // 更新用户信息
  updateProfile: (data) => api.put('/user/profile', data),
  
  // 获取用户统计信息
  getStats: () => api.get('/user/stats'),
  
  // 问候接口
  hello: () => api.get('/user/hello')
} 