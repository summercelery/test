<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="asideWidth" class="layout-aside">
      <div class="logo">
        <h2 v-if="!appStore.sidebarCollapsed">智能提醒系统</h2>
        <h2 v-else>智能</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :unique-opened="true"
        class="layout-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表板</template>
        </el-menu-item>
        
        <el-menu-item index="contacts">
          <el-icon><User /></el-icon>
          <template #title>联系人管理</template>
        </el-menu-item>
        
        <el-menu-item index="reminders">
          <el-icon><Bell /></el-icon>
          <template #title>提醒管理</template>
        </el-menu-item>
        
        <el-menu-item index="tags">
          <el-icon><PriceTag /></el-icon>
          <template #title>标签管理</template>
        </el-menu-item>
        
        <el-menu-item index="profile">
          <el-icon><UserFilled /></el-icon>
          <template #title>个人资料</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区域 -->
    <el-container class="layout-main">
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-button 
            :icon="appStore.sidebarCollapsed ? 'Expand' : 'Fold'"
            @click="appStore.toggleSidebar"
            text
            size="large"
          />
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item
              v-for="item in appStore.breadcrumb"
              :key="item.path"
              :to="item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-button
            :icon="appStore.theme === 'dark' ? 'Sunny' : 'Moon'"
            @click="toggleTheme"
            text
            size="large"
          />
          
          <el-dropdown @command="handleUserAction">
            <div class="user-dropdown">
              <el-avatar :size="32" :src="userInfo?.avatar">
                {{ userInfo?.fullName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo?.fullName || userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

// 计算属性
const activeMenu = computed(() => {
  const matched = route.matched
  if (matched.length > 0) {
    return matched[matched.length - 1].name?.toLowerCase() || 'dashboard'
  }
  return 'dashboard'
})

const asideWidth = computed(() => 
  appStore.sidebarCollapsed ? '64px' : '250px'
)

const userInfo = computed(() => userStore.userInfo)

// 方法
const handleMenuSelect = (index) => {
  router.push(`/${index}`)
}

const toggleTheme = () => {
  const newTheme = appStore.theme === 'light' ? 'dark' : 'light'
  appStore.setTheme(newTheme)
}

const handleUserAction = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logout()
        router.push('/login')
      } catch (error) {
        // 用户取消
      }
      break
  }
}

// 更新面包屑
const updateBreadcrumb = () => {
  const breadcrumb = []
  
  route.matched.forEach(match => {
    if (match.meta.title) {
      breadcrumb.push({
        title: match.meta.title,
        path: match.path
      })
    }
  })
  
  appStore.setBreadcrumb(breadcrumb)
}

onMounted(() => {
  updateBreadcrumb()
})

// 监听路由变化
watch(() => route.path, updateBreadcrumb)
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
}

.layout-aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e4e7ed;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
}

.layout-menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.layout-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s;
}

.user-dropdown:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
}

.layout-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-aside {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 1000;
    height: 100vh;
  }
  
  .layout-main {
    margin-left: 0;
  }
  
  .header-left {
    gap: 10px;
  }
  
  .breadcrumb {
    display: none;
  }
}

/* 暗色主题 */
.dark .layout-aside {
  background: #2d3748;
  border-right-color: #4a5568;
}

.dark .layout-header {
  background: #2d3748;
  border-bottom-color: #4a5568;
  color: #fff;
}

.dark .layout-content {
  background: #1a202c;
}

.dark .user-dropdown:hover {
  background: #4a5568;
}
</style> 