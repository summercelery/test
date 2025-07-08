import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 状态
  const sidebarCollapsed = ref(false)
  const theme = ref(localStorage.getItem('theme') || 'light')
  const loading = ref(false)
  const breadcrumb = ref([])

  // 动作
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setSidebarCollapsed = (collapsed) => {
    sidebarCollapsed.value = collapsed
  }

  const setTheme = (newTheme) => {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
    
    // 应用主题
    if (newTheme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  const setLoading = (isLoading) => {
    loading.value = isLoading
  }

  const setBreadcrumb = (items) => {
    breadcrumb.value = items
  }

  const addBreadcrumb = (item) => {
    breadcrumb.value.push(item)
  }

  const clearBreadcrumb = () => {
    breadcrumb.value = []
  }

  // 初始化主题
  const initializeTheme = () => {
    if (theme.value === 'dark') {
      document.documentElement.classList.add('dark')
    }
  }

  return {
    // 状态
    sidebarCollapsed,
    theme,
    loading,
    breadcrumb,
    
    // 动作
    toggleSidebar,
    setSidebarCollapsed,
    setTheme,
    setLoading,
    setBreadcrumb,
    addBreadcrumb,
    clearBreadcrumb,
    initializeTheme
  }
}) 