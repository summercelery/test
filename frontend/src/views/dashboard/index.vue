<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>欢迎回来，{{ userInfo?.fullName || userInfo?.username }}！</h1>
        <p>{{ getCurrentTimeGreeting() }}</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" @click="$router.push('/reminders/create')">
          <el-icon><Plus /></el-icon>
          创建提醒
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card total-reminders">
        <div class="stat-icon">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ stats.totalReminders || 0 }}</h3>
          <p>总提醒数</p>
        </div>
      </div>
      
      <div class="stat-card pending-reminders">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ stats.pendingReminders || 0 }}</h3>
          <p>待发送</p>
        </div>
      </div>
      
      <div class="stat-card contacts">
        <div class="stat-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ stats.contactCount || 0 }}</h3>
          <p>联系人</p>
        </div>
      </div>
      
      <div class="stat-card tags">
        <div class="stat-icon">
          <el-icon><PriceTag /></el-icon>
        </div>
        <div class="stat-content">
          <h3>{{ stats.tagCount || 0 }}</h3>
          <p>标签</p>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-container">
        <div class="chart-header">
          <h3>提醒状态分布</h3>
        </div>
        <div class="chart-content">
          <div class="progress-wrapper">
            <el-progress 
              type="circle" 
              :percentage="getCompletionPercentage()"
              :width="120"
              :stroke-width="8"
              :color="getProgressColor()"
            />
            <div class="progress-text">
              <div class="percentage">{{ getCompletionPercentage() }}%</div>
              <div class="description">{{ getStatusInfo() }}</div>
            </div>
          </div>
          <div class="chart-info">
            <div class="info-item" :class="{ active: stats.pendingReminders > 0 }">
              <span class="dot pending"></span>
              <span>待发送: {{ stats.pendingReminders || 0 }}</span>
            </div>
            <div class="info-item" :class="{ active: stats.sentReminders > 0 }">
              <span class="dot sent"></span>
              <span>已发送: {{ stats.sentReminders || 0 }}</span>
            </div>
            <div class="info-item" :class="{ active: stats.cancelledReminders > 0 }">
              <span class="dot cancelled"></span>
              <span>已取消: {{ stats.cancelledReminders || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="quick-actions">
        <div class="actions-header">
          <h3>快捷操作</h3>
        </div>
        <div class="actions-grid">
          <div class="action-item" @click="$router.push('/reminders/create')">
            <el-icon><Plus /></el-icon>
            <span>创建提醒</span>
          </div>
          <div class="action-item" @click="$router.push('/contacts/create')">
            <el-icon><UserFilled /></el-icon>
            <span>添加联系人</span>
          </div>
          <div class="action-item" @click="$router.push('/tags')">
            <el-icon><PriceTag /></el-icon>
            <span>管理标签</span>
          </div>
          <div class="action-item" @click="$router.push('/profile')">
            <el-icon><Setting /></el-icon>
            <span>个人设置</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近提醒 -->
    <div class="recent-section">
      <div class="section-header">
        <h3>最近提醒</h3>
        <el-link type="primary" @click="$router.push('/reminders')">查看全部</el-link>
      </div>
      
      <div class="recent-list" v-if="recentReminders.length > 0">
        <div 
          v-for="reminder in recentReminders" 
          :key="reminder.id"
          class="reminder-item"
        >
          <div class="reminder-info">
            <h4>{{ reminder.title }}</h4>
            <p>{{ reminder.content }}</p>
            <div class="reminder-meta">
              <span class="time">{{ formatTime(reminder.reminderTime) }}</span>
              <el-tag :type="getStatusTagType(reminder.status)" size="small">
                {{ reminder.status }}
              </el-tag>
            </div>
          </div>
          <div class="reminder-actions">
            <el-button 
              size="small" 
              text 
              @click="viewReminder(reminder.id)"
            >
              查看
            </el-button>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <el-icon class="empty-icon"><DocumentRemove /></el-icon>
        <p>还没有提醒记录</p>
        <el-button type="primary" @click="$router.push('/reminders/create')">
          创建第一个提醒
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi, remindersApi } from '@/api'
import { Plus, Bell, Clock, User, PriceTag, UserFilled, Setting, DocumentRemove } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const stats = ref({})
const recentReminders = ref([])
const isLoading = ref(false)

// 计算属性
const userInfo = computed(() => userStore.userInfo)

// 获取当前时间问候语
const getCurrentTimeGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息哦'
  if (hour < 12) return '上午好，开始美好的一天吧'
  if (hour < 18) return '下午好，继续努力工作'
  return '晚上好，今天辛苦了'
}

// 获取完成百分比
const getCompletionPercentage = () => {
  const total = stats.value.totalReminders || 0
  const sent = stats.value.sentReminders || 0
  if (total === 0) return 0
  return Math.round((sent / total) * 100)
}

// 获取进度条颜色
const getProgressColor = () => {
  const percentage = getCompletionPercentage()
  if (percentage === 0) return '#E6A23C' // 橙色，表示待处理
  if (percentage < 50) return '#F56C6C' // 红色，完成度低
  if (percentage < 80) return '#E6A23C' // 橙色，完成度中等
  return '#67C23A' // 绿色，完成度高
}

// 获取状态分布信息
const getStatusInfo = () => {
  const pending = stats.value.pendingReminders || 0
  const sent = stats.value.sentReminders || 0
  const cancelled = stats.value.cancelledReminders || 0
  const total = pending + sent + cancelled
  
  if (total === 0) {
    return '暂无提醒数据'
  }
  
  if (pending > 0 && sent === 0 && cancelled === 0) {
    return `${pending}个待发送提醒`
  }
  
  return `共${total}个提醒`
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '待发送':
      return ''
    case '已发送':
      return 'success'
    case '已取消':
      return 'info'
    case '发送失败':
      return 'danger'
    default:
      return ''
  }
}

// 查看提醒详情
const viewReminder = (id) => {
  router.push(`/reminders/${id}`)
}

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await userApi.getStats()
    console.log('获取的统计数据响应:', response);
    
    if (response.data && response.data.success) {
      stats.value = response.data.data;
      console.log('解析后的统计数据:', stats.value);
    } else {
      stats.value = response.data || {};
      console.log('直接使用响应数据:', stats.value);
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载最近提醒
const loadRecentReminders = async () => {
  try {
    const response = await remindersApi.getReminders({ page: 0, size: 5 })
    recentReminders.value = response.data.content || []
  } catch (error) {
    console.error('加载最近提醒失败:', error)
  }
}

// 初始化数据
const initData = async () => {
  isLoading.value = true
  try {
    await Promise.all([
      loadStats(),
      loadRecentReminders()
    ])
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  initData()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px;
  border-radius: 16px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-content h1 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
}

.welcome-content p {
  font-size: 16px;
  opacity: 0.9;
}

.welcome-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-weight: 500;
}

.welcome-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.total-reminders .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.pending-reminders .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.contacts .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.tags .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content h3 {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-content p {
  font-size: 14px;
  color: #7f8c8d;
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.chart-container,
.quick-actions {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.chart-header,
.actions-header {
  margin-bottom: 20px;
}

.chart-header h3,
.actions-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.chart-content {
  display: flex;
  align-items: center;
  gap: 30px;
}

.progress-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.progress-text {
  text-align: center;
}

.progress-text .percentage {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

.progress-text .description {
  font-size: 14px;
  color: #7f8c8d;
}

.chart-info {
  flex: 1;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.info-item.active {
  font-weight: 600;
  color: #2c3e50;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot.pending {
  background: #e6a23c;
}

.dot.sent {
  background: #67c23a;
}

.dot.cancelled {
  background: #909399;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.action-item .el-icon {
  font-size: 24px;
  color: #409eff;
}

.action-item span {
  font-size: 14px;
  color: #606266;
}

.recent-section {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.recent-list {
  space-y: 15px;
}

.reminder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.reminder-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.reminder-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.reminder-info p {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.reminder-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reminder-meta .time {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 15px;
  }
  
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .welcome-content h1 {
    font-size: 24px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .chart-content {
    flex-direction: column;
    text-align: center;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .reminder-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style> 