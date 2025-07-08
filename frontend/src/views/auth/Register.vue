<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>创建新账号</h1>
        <p>加入智能提醒管理系统</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item prop="phoneNumber">
          <el-input
            v-model="registerForm.phoneNumber"
            placeholder="请输入手机号"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item prop="fullName">
          <el-input
            v-model="registerForm.fullName"
            placeholder="请输入真实姓名"
            size="large"
            :prefix-icon="Avatar"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="verificationCode">
          <div class="sms-input-group">
            <el-input
              v-model="registerForm.verificationCode"
              placeholder="请输入验证码"
              size="large"
              :prefix-icon="Key"
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
        
        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <el-link type="primary" @click="showAgreement">《用户协议》</el-link>
            和
            <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
          </el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-button"
            :loading="userStore.isLoading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
      </div>
    </div>

    <!-- 用户协议对话框 -->
    <el-dialog v-model="agreementVisible" title="用户协议" width="60%">
      <div class="agreement-content">
        <h3>1. 服务条款</h3>
        <p>本智能提醒管理系统为用户提供提醒管理服务，用户在使用本服务前应仔细阅读本协议。</p>
        
        <h3>2. 用户权利与义务</h3>
        <p>用户有权使用本系统提供的所有功能，但应遵守相关法律法规，不得利用本系统进行违法活动。</p>
        
        <h3>3. 隐私保护</h3>
        <p>我们承诺保护用户的个人信息安全，不会将用户信息泄露给第三方。</p>
        
        <h3>4. 服务变更</h3>
        <p>本系统可能会根据业务需要对服务进行调整，我们会提前通知用户。</p>
        
        <h3>5. 免责声明</h3>
        <p>本系统不对因不可抗力等原因造成的服务中断承担责任。</p>
      </div>
      <template #footer>
        <el-button @click="agreementVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 隐私政策对话框 -->
    <el-dialog v-model="privacyVisible" title="隐私政策" width="60%">
      <div class="privacy-content">
        <h3>1. 信息收集</h3>
        <p>我们只收集提供服务所必需的用户信息，包括用户名、邮箱、手机号等。</p>
        
        <h3>2. 信息使用</h3>
        <p>收集的信息仅用于提供服务、改进用户体验和系统维护。</p>
        
        <h3>3. 信息保护</h3>
        <p>我们采用行业标准的安全措施保护用户信息，防止信息泄露。</p>
        
        <h3>4. 信息共享</h3>
        <p>未经用户同意，我们不会与第三方共享用户个人信息。</p>
        
        <h3>5. 用户权利</h3>
        <p>用户有权查看、修改或删除自己的个人信息。</p>
      </div>
      <template #footer>
        <el-button @click="privacyVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock, Phone, Message, Avatar, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const registerFormRef = ref()

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  phoneNumber: '',
  fullName: '',
  password: '',
  confirmPassword: '',
  verificationCode: '',
  agreement: false
})

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  fullName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度为2-10个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  agreement: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请同意用户协议和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 倒计时
const countdown = ref(0)
let countdownTimer = null

// 对话框显示状态
const agreementVisible = ref(false)
const privacyVisible = ref(false)

// 处理注册
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    await userStore.register(registerForm)
    
    // 注册成功，跳转到首页
    router.push('/')
  } catch (error) {
    // 验证失败或注册失败
  }
}

// 发送短信验证码
const sendSmsCode = async () => {
  if (!registerForm.phoneNumber) {
    ElMessage.error('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(registerForm.phoneNumber)) {
    ElMessage.error('手机号格式不正确')
    return
  }
  
  try {
    await userStore.sendSmsCode(registerForm.phoneNumber, 'REGISTER')
    startCountdown()
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

// 显示用户协议
const showAgreement = () => {
  agreementVisible.value = true
}

// 显示隐私政策
const showPrivacy = () => {
  privacyVisible.value = true
}

// 清理定时器
onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  max-height: 90vh;
  overflow-y: auto;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
}

.register-header p {
  font-size: 14px;
  color: #7f8c8d;
}

.register-form {
  margin-top: 20px;
}

.register-button {
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

.register-footer {
  text-align: center;
  margin-top: 30px;
  color: #7f8c8d;
  font-size: 14px;
}

.register-footer .el-link {
  margin-left: 5px;
}

.agreement-content,
.privacy-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
}

.agreement-content h3,
.privacy-content h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  margin-top: 20px;
}

.agreement-content p,
.privacy-content p {
  color: #7f8c8d;
  line-height: 1.6;
  margin-bottom: 15px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-box {
    padding: 30px 20px;
  }
  
  .register-header h1 {
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
.register-box {
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