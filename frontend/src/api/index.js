import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
let loadingInstance = null

api.interceptors.request.use(
  (config) => {
    // 显示加载指示器
    if (config.loading !== false) {
      loadingInstance = ElLoading.service({
        text: '请求中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }
    
    // 添加认证token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    if (loadingInstance) {
      loadingInstance.close()
    }
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    if (loadingInstance) {
      loadingInstance.close()
    }
    
    // 统一处理响应数据
    const { data } = response
    
    if (data.success) {
      return data
    } else {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  (error) => {
    if (loadingInstance) {
      loadingInstance.close()
    }
    
    const { response } = error
    
    if (response) {
      switch (response.status) {
        case 401:
          ElMessage.error('认证失败，请重新登录')
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

// 认证相关API
export const authApi = {
  // 用户登录
  login: (data) => api.post('/auth/login', data),
  
  // 用户注册
  register: (data) => api.post('/auth/register', data),
  
  // 用户退出
  logout: () => api.post('/auth/logout'),
  
  // 验证Token
  validate: () => api.get('/auth/validate'),
  
  // 发送短信验证码
  sendSms: (data) => api.post('/auth/send-sms', data),
  
  // 发送短信验证码（别名）
  sendSmsCode: (data) => api.post('/auth/send-sms', data),
  
  // 短信登录
  smsLogin: (data) => api.post('/auth/sms-login', data),
  
  // 重置密码
  resetPassword: (data) => api.post('/auth/reset-password', data),
  
  // 微信登录
  wechatLogin: (data) => api.post('/auth/wechat-login', data),
  
  // 获取微信授权URL
  getWechatAuthUrl: (params) => api.get('/auth/wechat/auth-url', { params })
}

// 用户相关API
export const userApi = {
  // 获取用户信息
  getUserInfo: () => api.get('/user/info'),
  
  // 更新用户信息
  updateUserInfo: (data) => api.put('/user/info', data),
  
  // 获取用户统计
  getUserStats: () => api.get('/user/stats'),
  
  // 获取用户统计（别名）
  getStats: () => api.get('/user/stats'),
  
  // 修改密码
  changePassword: (data) => api.post('/user/change-password', data)
}

// 联系人相关API
export const contactsApi = {
  // 获取联系人列表
  getContacts: (params) => api.get('/contacts', { params }),
  
  // 获取联系人详情
  getContact: (id) => api.get(`/contacts/${id}`),
  
  // 创建联系人
  createContact: (data) => api.post('/contacts', data),
  
  // 更新联系人
  updateContact: (id, data) => api.put(`/contacts/${id}`, data),
  
  // 删除联系人
  deleteContact: (id) => api.delete(`/contacts/${id}`),
  
  // 搜索联系人
  searchContacts: (name) => api.get('/contacts/search', { params: { name } }),
  
  // 根据标签获取联系人
  getContactsByTag: (tagId) => api.get(`/contacts/by-tag/${tagId}`),
  
  // 获取联系人统计
  getContactStats: () => api.get('/contacts/stats')
}

// 提醒相关API
export const remindersApi = {
  // 获取提醒列表
  getReminders: (params) => api.get('/reminders', { params }),
  
  // 获取提醒详情
  getReminder: (id) => api.get(`/reminders/${id}`),
  
  // 创建提醒
  createReminder: (data) => api.post('/reminders', data),
  
  // 更新提醒
  updateReminder: (id, data) => api.put(`/reminders/${id}`, data),
  
  // 取消提醒
  cancelReminder: (id) => api.put(`/reminders/${id}/cancel`),
  
  // 删除提醒
  deleteReminder: (id) => api.delete(`/reminders/${id}`),
  
  // 获取提醒统计
  getReminderStats: () => api.get('/reminders/stats'),
  
  // 获取提醒类型
  getReminderTypes: () => api.get('/reminders/types')
}

// 标签相关API
export const tagsApi = {
  // 获取标签列表
  getTags: () => api.get('/tags'),
  
  // 创建标签
  createTag: (data) => api.post('/tags', data),
  
  // 更新标签
  updateTag: (id, data) => api.put(`/tags/${id}`, data),
  
  // 删除标签
  deleteTag: (id) => api.delete(`/tags/${id}`)
}

export default api 