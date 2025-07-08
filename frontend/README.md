# 智能提醒管理系统 - 前端

基于 Vue 3 + Element Plus 的现代化提醒管理系统前端应用。

## 功能特性

### 🎯 核心功能
- **用户认证** - 支持账号密码登录、短信验证码登录、微信登录
- **仪表板** - 直观的数据统计和快捷操作
- **提醒管理** - 创建、编辑、删除和管理各类提醒
- **联系人管理** - 管理提醒接收人信息
- **标签系统** - 通过标签组织和分类数据
- **个人资料** - 用户信息管理和设置

### 🚀 技术特性
- **现代化技术栈** - Vue 3 + Composition API + Vite
- **组件库** - Element Plus UI 组件库
- **状态管理** - Pinia 状态管理
- **路由管理** - Vue Router 4
- **HTTP 客户端** - Axios 封装
- **响应式设计** - 支持移动端和桌面端
- **主题切换** - 支持明暗主题
- **国际化** - 中文界面

## 技术栈

- **框架**: Vue 3.3+
- **构建工具**: Vite 4.4+
- **UI 组件库**: Element Plus 2.3+
- **状态管理**: Pinia 2.1+
- **路由**: Vue Router 4.2+
- **HTTP 客户端**: Axios 1.5+
- **日期处理**: Day.js
- **工具库**: Lodash
- **开发语言**: JavaScript ES6+
- **样式**: SCSS

## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口封装
│   │   ├── index.js       # HTTP 客户端配置
│   │   ├── auth.js        # 认证相关接口
│   │   ├── user.js        # 用户相关接口
│   │   ├── contacts.js    # 联系人接口
│   │   ├── reminders.js   # 提醒接口
│   │   └── tags.js        # 标签接口
│   ├── components/        # 公共组件
│   ├── layout/           # 布局组件
│   │   └── index.vue     # 主布局
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义
│   ├── stores/           # 状态管理
│   │   ├── app.js        # 应用状态
│   │   └── user.js       # 用户状态
│   ├── styles/           # 样式文件
│   │   └── index.scss    # 全局样式
│   ├── views/            # 页面组件
│   │   ├── auth/         # 认证页面
│   │   ├── dashboard/    # 仪表板
│   │   ├── contacts/     # 联系人管理
│   │   ├── reminders/    # 提醒管理
│   │   ├── tags/         # 标签管理
│   │   ├── profile/      # 个人资料
│   │   └── error/        # 错误页面
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html             # HTML 模板
├── package.json           # 项目配置
├── vite.config.js         # Vite 配置
└── README.md              # 项目文档
```

## 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖

```bash
cd frontend
npm install
```

### 开发环境

```bash
# 启动开发服务器
npm run dev

# 或者
npm run serve
```

开发服务器将在 `http://localhost:5173` 启动，支持热重载。

### 生产构建

```bash
# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

### 代码检查

```bash
# 运行 ESLint
npm run lint
```

## 配置说明

### 后端 API 配置

在 `vite.config.js` 中配置后端 API 代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端服务地址
      changeOrigin: true,
      secure: false
    }
  }
}
```

### 环境变量

创建 `.env.local` 文件配置环境变量：

```bash
# API 基础地址（可选，默认使用代理）
VITE_API_BASE_URL=http://localhost:8080

# 应用标题
VITE_APP_TITLE=智能提醒管理系统
```

## 主要页面说明

### 1. 登录页面 (`/login`)
- 支持用户名/邮箱 + 密码登录
- 支持手机号 + 短信验证码登录
- 记住登录状态
- 找回密码功能

### 2. 注册页面 (`/register`)
- 用户信息注册
- 手机号验证
- 用户协议和隐私政策

### 3. 仪表板 (`/dashboard`)
- 数据统计概览
- 快捷操作入口
- 最近提醒列表
- 提醒状态分布图表

### 4. 提醒管理 (`/reminders`)
- 提醒列表查看和搜索
- 创建和编辑提醒
- 提醒状态管理
- 批量操作

### 5. 联系人管理 (`/contacts`)
- 联系人列表管理
- 添加和编辑联系人
- 标签分类
- 批量导入/导出

### 6. 标签管理 (`/tags`)
- 标签创建和管理
- 颜色分类
- 使用统计

### 7. 个人资料 (`/profile`)
- 个人信息修改
- 密码修改
- 偏好设置

## 开发指南

### 组件开发规范

1. 使用 Vue 3 Composition API
2. 采用 `<script setup>` 语法
3. 使用 Element Plus 组件库
4. 响应式设计适配移动端

### 状态管理

使用 Pinia 进行状态管理：

```javascript
// 使用用户状态
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isAuthenticated = userStore.isAuthenticated
```

### API 调用

统一使用封装的 API 客户端：

```javascript
import { authApi } from '@/api'

// 登录
const response = await authApi.login({ username, password })
```

### 路由守卫

自动处理认证状态：
- 需要登录的页面自动跳转到登录页
- 已登录用户访问登录页自动跳转到首页

## 部署指南

### 1. 构建生产版本

```bash
npm run build
```

### 2. 部署到 Web 服务器

将 `dist/` 目录下的文件部署到 Web 服务器（如 Nginx、Apache）。

### 3. Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;
    
    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理
    location /api {
        proxy_pass http://backend-server:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 常见问题

### 1. 开发服务器启动失败
- 检查 Node.js 版本是否符合要求
- 删除 `node_modules` 重新安装依赖

### 2. API 请求失败
- 检查后端服务是否启动
- 确认代理配置是否正确

### 3. 构建失败
- 检查代码语法错误
- 确认所有依赖已正确安装

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 创建 Pull Request

## 许可证

MIT License

## 更新日志

### v1.0.0 (2024-01-XX)
- 初始版本发布
- 完整的用户认证系统
- 提醒管理功能
- 联系人管理功能
- 标签系统
- 响应式设计
- 明暗主题支持 