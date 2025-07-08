import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由懒加载
const Login = () => import('@/views/auth/Login.vue')
const Register = () => import('@/views/auth/Register.vue')
const Layout = () => import('@/layout/index.vue')
const Dashboard = () => import('@/views/dashboard/index.vue')
const Contacts = () => import('@/views/contacts/index.vue')
const ContactForm = () => import('@/views/contacts/form.vue')
const Reminders = () => import('@/views/reminders/index.vue')
const ReminderForm = () => import('@/views/reminders/form.vue')
const Tags = () => import('@/views/tags/index.vue')
const Profile = () => import('@/views/profile/index.vue')
const NotFound = () => import('@/views/error/404.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false, title: '注册' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '仪表板', icon: 'Odometer' }
      },
      {
        path: 'contacts',
        name: 'Contacts',
        component: Contacts,
        meta: { title: '联系人管理', icon: 'User' }
      },
      {
        path: 'contacts/create',
        name: 'CreateContact',
        component: ContactForm,
        meta: { title: '新增联系人', icon: 'Plus' }
      },
      {
        path: 'contacts/:id/edit',
        name: 'EditContact',
        component: ContactForm,
        meta: { title: '编辑联系人', icon: 'Edit' }
      },
      {
        path: 'reminders',
        name: 'Reminders',
        component: Reminders,
        meta: { title: '提醒管理', icon: 'Bell' }
      },
      {
        path: 'reminders/create',
        name: 'CreateReminder',
        component: ReminderForm,
        meta: { title: '新增提醒', icon: 'Plus' }
      },
      {
        path: 'reminders/:id/edit',
        name: 'EditReminder',
        component: ReminderForm,
        meta: { title: '编辑提醒', icon: 'Edit' }
      },
      {
        path: 'tags',
        name: 'Tags',
        component: Tags,
        meta: { title: '标签管理', icon: 'PriceTag' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: { title: '个人资料', icon: 'UserFilled' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局导航守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智能提醒管理系统` : '智能提醒管理系统'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!userStore.isAuthenticated) {
      // 尝试从本地存储恢复用户状态
      await userStore.initializeAuth()
      
      if (!userStore.isAuthenticated) {
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
        return
      }
    }
  }
  
  // 如果已登录用户访问登录页面，重定向到首页
  if (userStore.isAuthenticated && (to.path === '/login' || to.path === '/register')) {
    next('/')
    return
  }
  
  next()
})

export default router 