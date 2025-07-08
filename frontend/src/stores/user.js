import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi } from '@/api'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const isLoading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)

  // 动作
  const login = async (credentials) => {
    try {
      isLoading.value = true
      const response = await authApi.login(credentials)
      
      // 后端返回的数据结构是 { success: true, message: "登录成功", data: { token, username, role } }
      token.value = response.data.token
      userInfo.value = {
        username: response.data.username,
        role: response.data.role
      }
      
      // 保存到本地存储
      localStorage.setItem('token', token.value)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      
      ElMessage.success('登录成功')
      return response.data
    } catch (error) {
      ElMessage.error(error.message || '登录失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const register = async (userData) => {
    try {
      isLoading.value = true
      
      // 过滤掉后端不需要的字段，只保留RegisterRequest期望的字段
      const filteredData = {
        username: userData.username,
        password: userData.password,
        email: userData.email,
        fullName: userData.fullName,
        phoneNumber: userData.phoneNumber,
        verificationCode: userData.verificationCode
      }
      
      const response = await authApi.register(filteredData)
      
      // 后端返回的数据结构是 { success: true, message: "注册成功", data: { token, username, role } }
      token.value = response.data.token
      userInfo.value = {
        username: response.data.username,
        role: response.data.role
      }
      
      // 保存到本地存储
      localStorage.setItem('token', token.value)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      
      ElMessage.success('注册成功')
      return response.data
    } catch (error) {
      ElMessage.error(error.message || '注册失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const smsLogin = async (credentials) => {
    try {
      isLoading.value = true
      const response = await authApi.smsLogin(credentials)
      
      // 后端返回的数据结构是 { success: true, message: "登录成功", data: { token, username, role } }
      token.value = response.data.token
      userInfo.value = {
        username: response.data.username,
        role: response.data.role
      }
      
      // 保存到本地存储
      localStorage.setItem('token', token.value)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      
      ElMessage.success('短信登录成功')
      return response.data
    } catch (error) {
      ElMessage.error(error.message || '短信登录失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const logout = async () => {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      // 清除状态
      token.value = ''
      userInfo.value = null
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      ElMessage.success('已退出登录')
    }
  }

  const initializeAuth = async () => {
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')
    
    if (savedToken && savedUserInfo && savedUserInfo !== 'undefined') {
      try {
        token.value = savedToken
        userInfo.value = JSON.parse(savedUserInfo)
        
        // 验证token是否有效
        try {
          const response = await authApi.validate()
          if (response.data.valid) {
            userInfo.value = response.data.user
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
          } else {
            await logout()
          }
        } catch (error) {
          await logout()
        }
      } catch (jsonError) {
        console.error('解析用户信息失败:', jsonError)
        await logout()
      }
    }
  }

  const updateProfile = async (profileData) => {
    try {
      isLoading.value = true
      const response = await userApi.updateProfile(profileData)
      
      userInfo.value = response.data
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      
      ElMessage.success('个人资料更新成功')
      return response.data
    } catch (error) {
      ElMessage.error(error.message || '更新个人资料失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const sendSmsCode = async (phoneNumber, smsType = 'LOGIN') => {
    try {
      isLoading.value = true
      await authApi.sendSmsCode({ phoneNumber, smsType })
      ElMessage.success('验证码发送成功')
    } catch (error) {
      ElMessage.error(error.message || '验证码发送失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const resetPassword = async (resetData) => {
    try {
      isLoading.value = true
      await authApi.resetPassword(resetData)
      ElMessage.success('密码重置成功')
    } catch (error) {
      ElMessage.error(error.message || '密码重置失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  return {
    // 状态
    token,
    userInfo,
    isLoading,
    
    // 计算属性
    isAuthenticated,
    
    // 动作
    login,
    register,
    smsLogin,
    logout,
    initializeAuth,
    updateProfile,
    sendSmsCode,
    resetPassword
  }
}) 