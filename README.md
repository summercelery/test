# 智能提醒系统前端

一个基于 Vue 3 的现代化智能提醒系统前端应用，提供优雅的用户界面和完整的提醒管理功能。

## ✨ 特性

- 🎨 **现代化设计** - 基于 Element Plus 的优雅界面设计
- 📱 **响应式布局** - 完美适配桌面和移动端
- 🌙 **主题切换** - 支持明暗主题切换
- 🔐 **完整认证** - 支持多种登录方式（账号密码、短信验证码）
- 📊 **数据可视化** - ECharts 图表展示统计数据
- 🔔 **提醒管理** - 完整的提醒创建、编辑和管理功能
- 👥 **联系人管理** - 便捷的联系人管理和标签分类
- ⚡ **高性能** - Vite 构建工具，开发体验更佳

## 🛠️ 技术栈

- **前端框架**: Vue 3 + Composition API
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router 4
- **HTTP 客户端**: Axios
- **图表库**: ECharts + Vue-ECharts
- **构建工具**: Vite
- **样式处理**: SCSS
- **日期处理**: Day.js

## 📋 系统要求

- Node.js >= 16.0.0
- npm >= 8.0.0

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd reminder-frontend
```

### 2. 安装依赖

```bash
npm install
```

### 3. 启动开发服务器

#### Windows 用户
```bash
start-dev.bat
```

#### Linux/Mac 用户
```bash
chmod +x start-dev.sh
./start-dev.sh
```

#### 或者直接使用 npm
```bash
npm run dev
```

### 4. 访问应用

打开浏览器访问: http://localhost:3000

## 📦 构建部署

### 构建生产版本

```bash
npm run build
```

### 预览构建结果

```bash
npm run preview
```

## 🏗️ 项目结构

```
reminder-frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口封装
│   │   ├── auth.js        # 认证相关接口
│   │   ├── contacts.js    # 联系人接口
│   │   ├── reminders.js   # 提醒接口
│   │   ├── user.js        # 用户接口
│   │   └── request.js     # HTTP 客户端配置
│   ├── components/        # 公共组件
│   │   └── Layout.vue     # 主布局组件
│   ├── router/            # 路由配置
│   │   └── index.js       # 路由定义
│   ├── stores/            # 状态管理
│   │   └── user.js        # 用户状态
│   ├── styles/            # 样式文件
│   │   └── index.scss     # 全局样式
│   ├── views/             # 页面组件
│   │   ├── auth/          # 认证相关页面
│   │   │   ├── Login.vue  # 登录页面
│   │   │   └── Register.vue # 注册页面
│   │   ├── Dashboard.vue  # 仪表板
│   │   ├── Contacts.vue   # 联系人管理
│   │   ├── Profile.vue    # 个人设置
│   │   └── 404.vue        # 404 页面
│   ├── App.vue            # 根组件
│   └── main.js            # 入口文件
├── index.html             # HTML 模板
├── vite.config.js         # Vite 配置
├── package.json           # 项目配置
├── start-dev.bat          # Windows 启动脚本
├── start-dev.sh           # Linux/Mac 启动脚本
└── README.md              # 项目说明
```

## 🔧 配置说明

### 环境配置

项目使用 Vite 的代理功能将 `/api` 请求转发到后端服务器：

```javascript
// vite.config.js
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      secure: false
    }
  }
}
```

### API 配置

所有 API 请求都通过统一的 HTTP 客户端处理，支持：

- 自动添加 Authorization 头
- 统一错误处理
- 响应拦截器
- 请求超时配置

## 🌟 主要功能

### 用户认证
- 用户登录（账号密码 + 短信验证码）
- 用户注册
- 密码重置
- 自动登录状态保持

### 仪表板
- 数据统计概览
- 提醒状态分布图表
- 最近提醒列表
- 快捷操作入口

### 联系人管理
- 联系人列表展示
- 搜索和筛选功能
- 联系人增删改查
- 标签分类管理

### 个人设置
- 基本信息编辑
- 使用统计查看
- 账户安全设置
- 个性化偏好配置

## 🎨 设计系统

### 色彩方案
- 主色调：渐变紫蓝色 (#667eea → #764ba2)
- 辅助色：绿色、橙色、红色等语义化颜色
- 中性色：灰色系列用于文本和背景

### 组件设计
- 卡片式布局
- 圆角设计
- 阴影效果
- 渐变按钮
- 响应式网格

## 🔌 API 接口

项目假设后端提供以下 API 接口：

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/contacts` - 获取联系人列表
- `POST /api/contacts` - 创建联系人
- `GET /api/reminders` - 获取提醒列表
- `POST /api/reminders` - 创建提醒
- `GET /api/user/profile` - 获取用户信息
- `GET /api/user/stats` - 获取用户统计

详细的 API 文档请参考后端项目文档。

## 🤝 开发规范

### 代码风格
- 使用 ESLint 进行代码检查
- 组件使用 PascalCase 命名
- 文件名使用 kebab-case
- 变量使用 camelCase

### 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建工具或依赖更新
```

## 📄 许可证

MIT License

## 🙋‍♂️ 联系方式

如有问题或建议，请联系：

- 邮箱：support@reminder.com
- GitHub Issues：请在项目 Issues 中提交

---

感谢使用智能提醒系统！ 🎉 