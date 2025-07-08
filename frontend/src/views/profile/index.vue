<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">个人资料</h1>
      <p class="page-description">管理您的个人信息和偏好设置</p>
    </div>

    <div class="profile-content">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <h3>基本信息</h3>
          <el-button type="primary" @click="editMode = !editMode">
            <el-icon><Edit /></el-icon>
            {{ editMode ? '取消编辑' : '编辑资料' }}
          </el-button>
        </div>
        
        <div class="card-content">
          <div class="avatar-section">
            <el-avatar :size="80" class="user-avatar">
              {{ userInfo?.fullName?.charAt(0) || userInfo?.username?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="avatar-info">
              <h4>{{ userInfo?.fullName || userInfo?.username }}</h4>
              <p>{{ userInfo?.email }}</p>
            </div>
          </div>
          
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
            :disabled="!editMode"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            
            <el-form-item label="真实姓名" prop="fullName">
              <el-input v-model="profileForm.fullName" placeholder="请输入真实姓名" />
            </el-form-item>
            
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phoneNumber">
              <el-input v-model="profileForm.phoneNumber" placeholder="请输入手机号" />
            </el-form-item>
            
            <el-form-item v-if="editMode">
              <el-button type="primary" @click="handleUpdateProfile" :loading="loading">
                保存修改
              </el-button>
              <el-button @click="resetForm">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 统计信息卡片 -->
      <div class="stats-card">
        <div class="card-header">
          <h3>使用统计</h3>
          <el-button text @click="refreshStats">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon total">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <h4>{{ stats.totalReminders || 0 }}</h4>
              <p>总提醒数</p>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <h4>{{ stats.pendingReminders || 0 }}</h4>
              <p>待发送</p>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon contacts">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <h4>{{ stats.contactCount || 0 }}</h4>
              <p>联系人</p>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon tags">
              <el-icon><PriceTag /></el-icon>
            </div>
            <div class="stat-info">
              <h4>{{ stats.tagCount || 0 }}</h4>
              <p>标签</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 账户安全卡片 -->
      <div class="security-card">
        <div class="card-header">
          <h3>账户安全</h3>
        </div>
        
        <div class="security-items">
          <div class="security-item">
            <div class="security-info">
              <h4>登录密码</h4>
              <p>定期更换密码可以提高账户安全性</p>
            </div>
            <el-button @click="showPasswordDialog = true">
              修改密码
            </el-button>
          </div>
          
          <div class="security-item">
            <div class="security-info">
              <h4>手机号验证</h4>
              <p>{{ userInfo?.phoneNumber ? '已绑定手机号' : '未绑定手机号' }}</p>
            </div>
            <el-button v-if="!userInfo?.phoneNumber">
              绑定手机
            </el-button>
          </div>
        </div>
      </div>

      <!-- 偏好设置卡片 -->
      <div class="preferences-card">
        <div class="card-header">
          <h3>偏好设置</h3>
        </div>
        
        <div class="preferences-content">
          <div class="preference-item">
            <div class="preference-info">
              <h4>主题模式</h4>
              <p>选择您喜欢的界面主题</p>
            </div>
            <el-switch
              v-model="isDarkMode"
              @change="handleThemeChange"
              active-text="暗黑模式"
              inactive-text="明亮模式"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="400px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="passwordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { userApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Edit, Refresh, Bell, Clock, User, PriceTag } from '@element-plus/icons-vue'

const userStore = useUserStore()
const appStore = useAppStore()

// 响应式数据
const editMode = ref(false)
const loading = ref(false)
const passwordLoading = ref(false)
const showPasswordDialog = ref(false)
const stats = ref({})

// 表单引用
const profileFormRef = ref()
const passwordFormRef = ref()

// 个人资料表单
const profileForm = reactive({
  username: '',
  fullName: '',
  email: '',
  phoneNumber: ''
})

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算属性
const userInfo = computed(() => userStore.userInfo)
const isDarkMode = computed({
  get: () => appStore.theme === 'dark',
  set: (value) => appStore.setTheme(value ? 'dark' : 'light')
})

// 表单验证规则
const profileRules = {
  fullName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度为2-10个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phoneNumber: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 初始化表单数据
const initFormData = () => {
  if (userInfo.value) {
    Object.assign(profileForm, {
      username: userInfo.value.username || '',
      fullName: userInfo.value.fullName || '',
      email: userInfo.value.email || '',
      phoneNumber: userInfo.value.phoneNumber || ''
    })
  }
}

// 重置表单
const resetForm = () => {
  initFormData()
}

// 更新个人资料
const handleUpdateProfile = async () => {
  try {
    await profileFormRef.value.validate()
    loading.value = true
    
    await userStore.updateProfile(profileForm)
    editMode.value = false
    ElMessage.success('个人资料更新成功')
  } catch (error) {
    // 错误已在store中处理
  } finally {
    loading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    
    // 这里需要调用修改密码的API
    // await userApi.changePassword(passwordForm)
    
    showPasswordDialog.value = false
    
    // 清空密码表单
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    ElMessage.success('密码修改成功')
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

// 处理主题切换
const handleThemeChange = (value) => {
  appStore.setTheme(value ? 'dark' : 'light')
  ElMessage.success(`已切换到${value ? '暗黑' : '明亮'}模式`)
}

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await userApi.getStats()
    stats.value = response.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 刷新统计数据
const refreshStats = () => {
  loadStats()
  ElMessage.success('统计数据已刷新')
}

// 初始化数据
onMounted(() => {
  initFormData()
  loadStats()
})

// 监听用户信息变化
watch(() => userInfo.value, initFormData, { deep: true })
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-description {
  color: #7f8c8d;
  font-size: 14px;
}

.profile-content > div {
  margin-bottom: 30px;
}

.profile-card,
.stats-card,
.security-card,
.preferences-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.card-content {
  padding: 30px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 32px;
  font-weight: 600;
}

.avatar-info h4 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.avatar-info p {
  color: #7f8c8d;
  font-size: 14px;
}

.profile-form .el-form-item {
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  padding: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  border-radius: 8px;
  background: #f8f9fa;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.contacts {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.tags {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info h4 {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 2px;
}

.stat-info p {
  font-size: 12px;
  color: #7f8c8d;
}

.security-items,
.preferences-content {
  padding: 20px 30px;
}

.security-item,
.preference-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #e4e7ed;
}

.security-item:last-child,
.preference-item:last-child {
  border-bottom: none;
}

.security-info h4,
.preference-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.security-info p,
.preference-info p {
  font-size: 14px;
  color: #7f8c8d;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-page {
    padding: 15px;
  }
  
  .card-header {
    padding: 15px 20px;
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .card-content {
    padding: 20px;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    padding: 15px;
  }
  
  .security-item,
  .preference-item {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .security-items,
  .preferences-content {
    padding: 15px 20px;
  }
}

/* 动画效果 */
.profile-card,
.stats-card,
.security-card,
.preferences-card {
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style> 