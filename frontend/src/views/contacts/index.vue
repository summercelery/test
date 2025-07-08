<template>
  <div class="contacts-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">联系人管理</h1>
        <p class="page-description">管理您的提醒联系人信息</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="$router.push('/contacts/create')">
          <el-icon><Plus /></el-icon>
          添加联系人
        </el-button>
      </div>
    </div>

    <!-- 搜索和过滤 -->
    <div class="filter-section">
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索联系人姓名或手机号"
          @input="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="filter-controls">
        <el-select v-model="selectedTag" placeholder="按标签筛选" clearable @change="handleTagFilter">
          <el-option label="全部标签" value="" />
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 联系人列表 -->
    <div class="contacts-content">
      <div v-if="loading" class="loading-container">
        <el-loading text="加载中..." />
      </div>
      
      <div v-else-if="contacts.length === 0" class="empty-state">
        <el-icon class="empty-icon"><UserFilled /></el-icon>
        <h3>暂无联系人</h3>
        <p>开始添加您的第一个联系人吧</p>
        <el-button type="primary" @click="$router.push('/contacts/create')">
          添加联系人
        </el-button>
      </div>
      
      <div v-else class="contacts-grid">
        <div
          v-for="contact in contacts"
          :key="contact.id"
          class="contact-card"
        >
          <div class="contact-avatar">
            <el-avatar :size="48">
              {{ contact.name.charAt(0) }}
            </el-avatar>
          </div>
          
          <div class="contact-info">
            <h3 class="contact-name">{{ contact.name }}</h3>
            <div class="contact-details">
              <div v-if="contact.phoneNumber" class="detail-item">
                <el-icon><Phone /></el-icon>
                <span>{{ contact.phoneNumber }}</span>
              </div>
              <div v-if="contact.wechatOpenid" class="detail-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>微信已绑定</span>
              </div>
            </div>
            <div v-if="contact.tags && contact.tags.length > 0" class="contact-tags">
              <el-tag
                v-for="tag in contact.tags"
                :key="tag.id"
                size="small"
                class="tag-item"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </div>
          
          <div class="contact-actions">
            <el-dropdown @command="handleAction">
              <el-button text>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="`edit-${contact.id}`">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item :command="`delete-${contact.id}`" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="pagination.total > 0 && !searchQuery" class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { contactsApi, tagsApi } from '@/api'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Search, Refresh, UserFilled, Phone, ChatDotRound, MoreFilled, Edit, Delete } from '@element-plus/icons-vue'

const router = useRouter()

// 响应式数据
const contacts = ref([])
const tags = ref([])
const loading = ref(false)
const searchQuery = ref('')
const selectedTag = ref('')

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 搜索防抖定时器
let searchTimer = null

// 加载联系人列表
const loadContacts = async () => {
  try {
    loading.value = true
    
    let response
    if (searchQuery.value) {
      // 使用搜索接口
      response = await contactsApi.searchContacts(searchQuery.value)
      contacts.value = response.data || []
      pagination.total = contacts.value.length
    } else {
      // 使用分页接口
      const params = {
        page: pagination.currentPage - 1,
        size: pagination.pageSize
      }
      response = await contactsApi.getContacts(params)
      contacts.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载联系人列表失败')
  } finally {
    loading.value = false
  }
}

// 加载标签列表
const loadTags = async () => {
  try {
    const response = await tagsApi.getTags()
    tags.value = response.data || []
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 处理搜索
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.currentPage = 1
    loadContacts()
  }, 500)
}

// 处理标签筛选
const handleTagFilter = async () => {
  try {
    if (selectedTag.value) {
      loading.value = true
      const response = await contactsApi.getContactsByTag(selectedTag.value)
      contacts.value = response.data || []
      pagination.total = contacts.value.length
    } else {
      loadContacts()
    }
  } catch (error) {
    ElMessage.error('按标签筛选失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  searchQuery.value = ''
  selectedTag.value = ''
  pagination.currentPage = 1
  loadContacts()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadContacts()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadContacts()
}

// 处理操作
const handleAction = async (command) => {
  const [action, id] = command.split('-')
  
  switch (action) {
    case 'edit':
      router.push(`/contacts/${id}/edit`)
      break
    case 'delete':
      await handleDelete(parseInt(id))
      break
  }
}

// 删除联系人
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个联系人吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await contactsApi.deleteContact(id)
    ElMessage.success('联系人删除成功')
    
    // 重新加载数据
    loadContacts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除联系人失败')
    }
  }
}

// 初始化数据
onMounted(() => {
  loadContacts()
  loadTags()
})
</script>

<style scoped>
.contacts-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left h1 {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-description {
  color: #7f8c8d;
  font-size: 14px;
}

.header-actions .el-button {
  height: 44px;
  padding: 0 24px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
}

.filter-section {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  align-items: center;
}

.search-box {
  flex: 1;
  max-width: 400px;
}

.filter-controls {
  display: flex;
  gap: 15px;
  align-items: center;
}

.contacts-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 30px;
  min-height: 400px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  color: #dcdfe6;
}

.empty-state h3 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #606266;
}

.empty-state p {
  font-size: 14px;
  margin-bottom: 30px;
}

.contacts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.contact-card {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: #fafafa;
}

.contact-card:hover {
  border-color: #409eff;
  background: #f0f9ff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.contact-avatar {
  margin-right: 15px;
}

.contact-info {
  flex: 1;
}

.contact-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.contact-details {
  margin-bottom: 10px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  font-size: 13px;
  color: #606266;
}

.detail-item .el-icon {
  font-size: 14px;
  color: #909399;
}

.contact-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-item {
  font-size: 12px;
}

.contact-actions {
  margin-left: 10px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .contacts-page {
    padding: 15px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 15px;
  }
  
  .search-box {
    max-width: none;
  }
  
  .filter-controls {
    width: 100%;
    justify-content: center;
  }
  
  .contacts-grid {
    grid-template-columns: 1fr;
  }
  
  .contact-card {
    flex-direction: column;
    text-align: center;
  }
  
  .contact-avatar {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .contact-actions {
    margin-left: 0;
    margin-top: 15px;
  }
}

/* 动画效果 */
.contact-card {
  animation: fadeInUp 0.5s ease-out;
}

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
</style> 