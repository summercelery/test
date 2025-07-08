import api from './index'

// 认证相关API
export const authApi = {
  // 登录
  login: (data) => api.post('/auth/login', data),
  
  // 注册
  register: (data) => api.post('/auth/register', data),
  
  // 短信验证码登录
  smsLogin: (data) => api.post('/auth/sms-login', data),
  
  // 发送短信验证码
  sendSmsCode: (data) => api.post('/auth/send-sms', data),
  
  // 重置密码
  resetPassword: (data) => api.post('/auth/reset-password', data),
  
  // 微信登录
  wechatLogin: (data) => api.post('/auth/wechat-login', data),
  
  // 获取微信授权URL
  getWechatAuthUrl: (params) => api.get('/auth/wechat/auth-url', { params }),
  
  // 验证token
  validateToken: () => api.get('/auth/validate'),
  
  // 退出登录
  logout: () => api.post('/auth/logout'),
  
  // 测试接口
  test: () => api.get('/auth/test')
} 