# 用户认证系统前端

这是一个现代化的用户认证系统前端界面，使用HTML、CSS和JavaScript构建，可以部署在Nginx上。

## 功能特性

- 🎨 现代化UI设计，响应式布局
- 🔐 手机号+邮箱+密码登录和注册
- 📱 注册模态框弹出设计
- 👤 用户仪表板和个人资料
- 🔑 JWT Token认证 + Redis会话管理
- 📱 移动端完美适配
- 🎯 居中美观的消息提示系统
- 🔄 自动Token验证和会话检查
- 🔒 密码强度实时检测
- 📞 手机号自动格式化
- 👁️ 密码显示/隐藏切换
- ⚡ 按钮加载状态动画
- 🎭 背景浮动动画效果
- 📊 会话统计和管理功能
- ⏰ 会话时间延长和过期提醒
- 🛡️ 强制下线其他设备
- 🔍 实时会话状态监控

## 文件结构

```
frontend/
├── index.html          # 主页面
├── styles.css          # 样式文件
├── script.js           # JavaScript逻辑
├── nginx.conf          # Nginx配置
├── Dockerfile          # Docker配置
└── README.md           # 说明文档
```

## 部署方式

### 方式一：直接部署到Nginx

1. **安装Nginx**
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install nginx
   
   # CentOS/RHEL
   sudo yum install nginx
   ```

2. **复制文件**
   ```bash
   # 复制前端文件到Nginx目录
   sudo cp index.html /var/www/html/
   sudo cp styles.css /var/www/html/
   sudo cp script.js /var/www/html/
   
   # 复制Nginx配置
   sudo cp nginx.conf /etc/nginx/sites-available/user-auth
   sudo ln -s /etc/nginx/sites-available/user-auth /etc/nginx/sites-enabled/
   ```

3. **重启Nginx**
   ```bash
   sudo nginx -t  # 测试配置
   sudo systemctl restart nginx
   ```

### 方式二：使用Docker部署

1. **构建镜像**
   ```bash
   cd frontend
   docker build -t user-auth-frontend .
   ```

2. **运行容器**
   ```bash
   docker run -d -p 80:80 --name frontend user-auth-frontend
   ```

### 方式三：使用Docker Compose（推荐）

在项目根目录运行：
```bash
docker-compose up -d
```

这将同时启动：
- 前端服务（端口80）
- 后端服务（端口8080）
- MySQL数据库（端口3306）

## 配置说明

### API地址配置

在 `script.js` 文件中修改API基础URL：

```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

如果您的后端运行在不同的地址，请相应修改。

### Nginx配置

`nginx.conf` 文件包含以下配置：

- 静态文件服务
- API代理到后端
- CORS支持
- Gzip压缩
- 安全头设置

### 环境要求

- Nginx 1.18+
- 现代浏览器（Chrome、Firefox、Safari、Edge）
- 后端Spring Boot服务运行在8080端口

## 使用说明

1. **访问应用**
   - 打开浏览器访问 `http://localhost`

2. **用户登录**
   - 在登录页面输入手机号和密码
   - 点击"登录"按钮
   - 登录成功后自动跳转到仪表板

3. **注册新用户**
   - 在登录页面点击"立即注册"
   - 在弹出的模态框中填写手机号和密码
   - 确认密码并同意服务条款
   - 提交注册

4. **使用仪表板**
   - 查看个人资料信息
   - 测试认证功能
   - 管理账户设置

5. **高级功能**
   - 密码强度实时检测
   - 手机号自动格式化
   - 邮箱格式实时验证
   - 密码显示/隐藏切换
   - 居中美观的错误提示
   - 记住登录状态
   - 会话统计和管理
   - 会话时间延长
   - 强制下线其他设备
   - 会话过期提醒

## 故障排除

### 常见问题

1. **API连接失败**
   - 检查后端服务是否运行
   - 确认API地址配置正确
   - 检查防火墙设置

2. **CORS错误**
   - 确认Nginx配置中的CORS头设置
   - 检查后端CORS配置

3. **静态文件404**
   - 确认文件路径正确
   - 检查Nginx配置中的root目录

### 日志查看

```bash
# Nginx错误日志
sudo tail -f /var/log/nginx/error.log

# Nginx访问日志
sudo tail -f /var/log/nginx/access.log

# Docker容器日志
docker logs frontend
```

## 开发说明

### 本地开发

1. 启动本地HTTP服务器：
   ```bash
   # 使用Python
   python -m http.server 8000
   
   # 使用Node.js
   npx http-server -p 8000
   ```

2. 修改API地址为本地后端地址

3. 访问 `http://localhost:8000`

### 自定义样式

修改 `styles.css` 文件来自定义界面样式：

- 颜色主题
- 布局调整
- 响应式断点
- 动画效果

### 功能扩展

在 `script.js` 中添加新功能：

- 新的API调用
- 表单验证
- 数据展示
- 用户交互

## 安全考虑

- 使用HTTPS在生产环境中
- 配置适当的CORS策略
- 设置安全头
- 定期更新依赖

## 许可证

MIT License 