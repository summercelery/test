<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-illustration">
        <div class="error-number">404</div>
        <div class="error-message">页面不存在</div>
      </div>
      
      <div class="error-description">
        <h3>抱歉，您访问的页面不存在</h3>
        <p>可能是页面地址错误，或者页面已被删除</p>
      </div>
      
      <div class="error-actions">
        <el-button type="primary" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.error-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.error-content {
  text-align: center;
  max-width: 500px;
  background: white;
  padding: 60px 40px;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.error-illustration {
  margin-bottom: 40px;
}

.error-number {
  font-size: 120px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.error-message {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.error-description {
  margin-bottom: 40px;
}

.error-description h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
}

.error-description p {
  font-size: 14px;
  color: #7f8c8d;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.error-actions .el-button {
  height: 44px;
  padding: 0 24px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .error-content {
    padding: 40px 20px;
  }
  
  .error-number {
    font-size: 80px;
  }
  
  .error-message {
    font-size: 20px;
  }
  
  .error-actions {
    flex-direction: column;
  }
}

/* 动画效果 */
.error-content {
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