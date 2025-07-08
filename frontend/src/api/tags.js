import api from './index'

// 标签相关API
export const tagsApi = {
  // 获取标签列表
  getTags: () => api.get('/tags'),
  
  // 搜索标签
  searchTags: (params) => api.get('/tags/search', { params }),
  
  // 获取标签详情
  getTag: (id) => api.get(`/tags/${id}`),
  
  // 创建标签
  createTag: (data) => api.post('/tags', data),
  
  // 更新标签
  updateTag: (id, data) => api.put(`/tags/${id}`, data),
  
  // 删除标签
  deleteTag: (id) => api.delete(`/tags/${id}`),
  
  // 获取标签统计信息
  getStats: () => api.get('/tags/stats')
} 