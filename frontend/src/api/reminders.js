import api from './index'

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
  cancelReminder: (id) => api.post(`/reminders/${id}/cancel`),
  
  // 删除提醒
  deleteReminder: (id) => api.delete(`/reminders/${id}`),
  
  // 获取提醒统计信息
  getStats: () => api.get('/reminders/stats'),
  
  // 获取提醒类型列表
  getTypes: () => api.get('/reminders/types')
} 