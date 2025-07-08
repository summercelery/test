<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>智能提醒管理系统</h1>
        <p>欢迎回来，请登录您的账号</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 账号密码登录 -->
        <el-tab-pane label="账号登录" name="password">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名或邮箱"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <div class="form-options">
                <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
                <el-link type="primary" @click="showResetPassword">忘记密码？</el-link>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="userStore.isLoading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 短信登录 -->
        <el-tab-pane label="短信登录" name="sms">
          <el-form
            ref="smsFormRef"
            :model="smsForm"
            :rules="smsRules"
            class="login-form"
            @keyup.enter="handleSmsLogin"
          >
            <el-form-item prop="phoneNumber">
              <el-input
                v-model="smsForm.phoneNumber"
                placeholder="请输入手机号"
                size="large"
                :prefix-icon="Phone"
              />
            </el-form-item>
            <el-form-item prop="verificationCode">
              <div class="sms-input-group">
                <el-input
                  v-model="smsForm.verificationCode"
                  placeholder="请输入验证码"
                  size="large"
                  :prefix-icon="Message"
                />
                <el-button
                  :disabled="countdown > 0"
                  @click="sendSmsCode"
                  size="large"
                  class="sms-button"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="userStore.isLoading"
                @click="handleSmsLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
    </div>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordVisible"
      title="重置密码"
      width="400px"
    >
      <el-form
        ref="resetFormRef"
        :model="resetForm"
        :rules="resetRules"
        label-width="80px"
      >
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input
            v-model="resetForm.phoneNumber"
            placeholder="请输入注册时的手机号"
          />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div class="sms-input-group">
            <el-input
              v-model="resetForm.verificationCode"
              placeholder="请输入验证码"
            />
            <el-button
              :disabled="resetCountdown > 0"
              @click="sendResetSmsCode"
              size="small"
            >
              {{ resetCountdown > 0 ? `${resetCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock, Phone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref()
const smsFormRef = ref()
const resetFormRef = ref()

// 活动标签
const activeTab = ref('password')

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 短信登录表单
const smsForm = reactive({
  phoneNumber: '',
  verificationCode: ''
})

// 重置密码表单
const resetForm = reactive({
  phoneNumber: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const smsRules = {
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}

const resetRules = {
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 倒计时相关
const countdown = ref(0)
const resetCountdown = ref(0)
const resetPasswordVisible = ref(false)

let countdownTimer = null
let resetCountdownTimer = null

// 处理登录
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    await userStore.login(loginForm)
    
    // 跳转到目标页面
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    // 验证失败或登录失败
  }
}

// 处理短信登录
const handleSmsLogin = async () => {
  try {
    await smsFormRef.value.validate()
    await userStore.smsLogin(smsForm)
    
    // 跳转到目标页面
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    // 验证失败或登录失败
  }
}

// 发送短信验证码
const sendSmsCode = async () => {
  if (!smsForm.phoneNumber) {
    ElMessage.error('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(smsForm.phoneNumber)) {
    ElMessage.error('手机号格式不正确')
    return
  }
  
  try {
    await userStore.sendSmsCode(smsForm.phoneNumber, 'LOGIN')
    startCountdown()
  } catch (error) {
    // 错误已在store中处理
  }
}

// 发送重置密码验证码
const sendResetSmsCode = async () => {
  if (!resetForm.phoneNumber) {
    ElMessage.error('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(resetForm.phoneNumber)) {
    ElMessage.error('手机号格式不正确')
    return
  }
  
  try {
    await userStore.sendSmsCode(resetForm.phoneNumber, 'RESET_PASSWORD')
    startResetCountdown()
  } catch (error) {
    // 错误已在store中处理
  }
}

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

// 开始重置倒计时
const startResetCountdown = () => {
  resetCountdown.value = 60
  resetCountdownTimer = setInterval(() => {
    resetCountdown.value--
    if (resetCountdown.value <= 0) {
      clearInterval(resetCountdownTimer)
    }
  }, 1000)
}

// 显示重置密码对话框
const showResetPassword = () => {
  resetPasswordVisible.value = true
}

// 处理重置密码
const handleResetPassword = async () => {
  try {
    await resetFormRef.value.validate()
    await userStore.resetPassword(resetForm)
    resetPasswordVisible.value = false
    
    // 清空表单
    Object.assign(resetForm, {
      phoneNumber: '',
      verificationCode: '',
      newPassword: '',
      confirmPassword: ''
    })
  } catch (error) {
    // 验证失败或重置失败
  }
}

// 清理定时器
onMounted(() => {
  return () => {
    if (countdownTimer) {
      clearInterval(countdownTimer)
    }
    if (resetCountdownTimer) {
      clearInterval(resetCountdownTimer)
    }
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
}

.login-header p {
  font-size: 14px;
  color: #7f8c8d;
}

.login-tabs {
  margin-bottom: 20px;
}

.login-form {
  margin-top: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
}

.sms-input-group {
  display: flex;
  gap: 10px;
  width: 100%;
}

.sms-input-group .el-input {
  flex: 1;
}

.sms-button {
  white-space: nowrap;
  min-width: 100px;
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  color: #7f8c8d;
  font-size: 14px;
}

.login-footer .el-link {
  margin-left: 5px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
  }
  
  .login-header h1 {
    font-size: 20px;
  }
  
  .sms-input-group {
    flex-direction: column;
  }
  
  .sms-button {
    min-width: auto;
  }
}

/* 动画效果 */
.login-box {
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