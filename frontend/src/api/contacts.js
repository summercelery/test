import api from './index'

// 联系人相关API
export const contactsApi = {
  // 获取联系人列表（分页）
  getContacts: (params) => api.get('/contacts', { params }),
  
  // 获取所有联系人
  getAllContacts: () => api.get('/contacts/all'),
  
  // 根据标签获取联系人
  getContactsByTag: (tagId) => api.get(`/contacts/by-tag/${tagId}`),
  
  // 搜索联系人
  searchContacts: (name) => api.get('/contacts/search', { params: { name } }),
  
  // 获取联系人详情
  getContact: (id) => api.get(`/contacts/${id}`),
  
  // 创建联系人
  createContact: (data) => api.post('/contacts', data),
  
  // 更新联系人
  updateContact: (id, data) => api.put(`/contacts/${id}`, data),
  
  // 删除联系人
  deleteContact: (id) => api.delete(`/contacts/${id}`),
  
  // 获取联系人统计信息
  getStats: () => api.get('/contacts/stats')
} 