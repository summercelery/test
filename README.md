# 用户认证系统

一个基于Spring Boot的完整用户认证系统，包含前端页面和后端API，支持多种登录方式、定时提醒、提醒人管理等功能。

## 功能特性

### 用户认证
- ✅ 用户名密码登录
- ✅ 短信验证码登录
- ✅ 微信登录
- ✅ 用户注册（手机号+邮箱+密码）
- ✅ 忘记密码（短信验证码重置）
- ✅ JWT令牌认证
- ✅ 会话管理（Redis存储）

### 定时提醒系统
- ✅ 创建定时提醒（一次性、每日、每周、每月重复）
- ✅ 多种提醒方式（微信、短信、电话）
- ✅ 提醒人管理（支持手机号和微信OpenID）
- ✅ 标签管理（为提醒人添加标签）
- ✅ 自动发送提醒（定时任务）
- ✅ 提醒统计和管理

### 微信集成
- ✅ 微信登录
- ✅ 微信公众号消息接收和回复
- ✅ 微信扫码获取OpenID
- ✅ Access Token自动更新（定时任务）

### 系统功能
- ✅ 定时任务管理
- ✅ Redis缓存支持
- ✅ 数据库支持（H2/MySQL）
- ✅ Docker部署支持
- ✅ Nginx配置

## 技术栈

### 后端
- **Spring Boot 2.7.x** - 主框架
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据访问
- **Spring Data Redis** - 缓存
- **JWT** - 令牌认证
- **MySQL/H2** - 数据库
- **Maven** - 依赖管理

### 前端
- **HTML5/CSS3** - 页面结构
- **JavaScript (ES6+)** - 交互逻辑
- **Bootstrap 5** - UI框架
- **Font Awesome** - 图标库

### 部署
- **Docker** - 容器化
- **Docker Compose** - 服务编排
- **Nginx** - 反向代理

## 项目结构

```
├── src/main/java/com/example/
│   ├── config/                    # 配置类
│   │   ├── SecurityConfig.java    # 安全配置
│   │   └── SchedulingConfig.java  # 定时任务配置
│   ├── controller/                # 控制器
│   │   ├── AuthController.java    # 认证控制器
│   │   ├── UserController.java    # 用户控制器
│   │   ├── ReminderController.java # 提醒控制器
│   │   ├── ContactController.java # 提醒人控制器
│   │   ├── TagController.java     # 标签控制器
│   │   └── WechatController.java  # 微信控制器
│   ├── service/                   # 服务层
│   │   ├── AuthService.java       # 认证服务
│   │   ├── UserService.java       # 用户服务
│   │   ├── ReminderService.java   # 提醒服务
│   │   ├── ContactService.java    # 提醒人服务
│   │   ├── TagService.java        # 标签服务
│   │   ├── WechatService.java     # 微信服务
│   │   ├── SmsService.java        # 短信服务
│   │   └── PhoneService.java      # 电话服务
│   ├── repository/                # 数据访问层
│   │   ├── UserRepository.java    # 用户Repository
│   │   ├── ReminderRepository.java # 提醒Repository
│   │   ├── ContactRepository.java # 提醒人Repository
│   │   └── TagRepository.java     # 标签Repository
│   ├── entity/                    # 实体类
│   │   ├── User.java              # 用户实体
│   │   ├── Reminder.java          # 提醒实体
│   │   ├── Contact.java           # 提醒人实体
│   │   └── Tag.java               # 标签实体
│   ├── dto/                       # 数据传输对象
│   │   ├── LoginRequest.java      # 登录请求
│   │   ├── RegisterRequest.java   # 注册请求
│   │   └── ...                    # 其他DTO
│   ├── security/                  # 安全相关
│   │   ├── JwtTokenUtil.java      # JWT工具
│   │   └── JwtRequestFilter.java  # JWT过滤器
│   ├── task/                      # 定时任务
│   │   └── ScheduledTasks.java    # 定时任务类
│   └── util/                      # 工具类
│       └── SecurityUtil.java      # 安全工具
├── frontend/                      # 前端文件
│   ├── index.html                 # 主页面
│   ├── login.html                 # 登录页面
│   ├── profile.html               # 个人资料页面
│   ├── contacts.html              # 提醒人管理页面
│   ├── reminders.html             # 提醒管理页面
│   ├── script.js                  # 主要JavaScript
│   ├── styles.css                 # 样式文件
│   └── ...                        # 其他前端文件
├── docker-compose.yml             # Docker编排文件
├── Dockerfile                     # 后端Dockerfile
├── nginx.conf                     # Nginx配置
└── README.md                      # 项目说明
```

## 定时任务功能

### 1. Access Token自动更新
- **执行频率**: 每1小时55分钟
- **功能**: 自动刷新微信Access Token，确保API调用正常

### 2. 提醒发送任务
- **执行频率**: 每分钟
- **功能**: 检查并发送到期的提醒

### 3. 会话清理任务
- **执行频率**: 每30分钟
- **功能**: 清理过期的用户会话

### 4. 系统健康检查
- **执行频率**: 每5分钟
- **功能**: 检查系统健康状态

## 快速开始

### 1. 环境要求
- Java 8+
- Maven 3.6+
- MySQL 5.7+ 或 H2
- Redis 5.0+
- Node.js 14+ (可选，用于前端开发)

### 2. 克隆项目
```bash
git clone <repository-url>
cd user-auth-system
```

### 3. 配置数据库
```bash
# 执行建表脚本
mysql -u username -p < database_schema.sql
```

### 4. 配置应用
编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_auth_system
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    
wechat:
  app-id: your_wechat_app_id
  app-secret: your_wechat_app_secret
  token: your_wechat_token

sms:
  api-key: your_sms_api_key
  api-secret: your_sms_api_secret
```

### 5. 启动应用
```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

### 6. 访问应用
- 后端API: http://localhost:8080
- 前端页面: http://localhost:8080/frontend/

## Docker部署

### 1. 构建镜像
```bash
# 构建后端镜像
docker build -t user-auth-backend .

# 构建前端镜像
cd frontend
docker build -t user-auth-frontend .
```

### 2. 使用Docker Compose启动
```bash
docker-compose up -d
```

### 3. 访问应用
- 前端: http://localhost:80
- 后端API: http://localhost:8080

## API文档

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/sms-login` - 短信登录
- `POST /api/auth/send-sms` - 发送短信验证码
- `POST /api/auth/reset-password` - 重置密码
- `POST /api/auth/logout` - 用户登出

### 提醒相关
- `POST /api/reminders` - 创建提醒
- `GET /api/reminders` - 获取提醒列表
- `GET /api/reminders/{id}` - 获取提醒详情
- `POST /api/reminders/{id}/cancel` - 取消提醒
- `DELETE /api/reminders/{id}` - 删除提醒
- `GET /api/reminders/stats` - 获取提醒统计

### 提醒人相关
- `POST /api/contacts` - 创建提醒人
- `GET /api/contacts` - 获取提醒人列表
- `GET /api/contacts/all` - 获取所有提醒人
- `GET /api/contacts/by-tag/{tagId}` - 根据标签获取提醒人
- `GET /api/contacts/search` - 搜索提醒人
- `PUT /api/contacts/{id}` - 更新提醒人
- `DELETE /api/contacts/{id}` - 删除提醒人

### 标签相关
- `POST /api/tags` - 创建标签
- `GET /api/tags` - 获取标签列表
- `GET /api/tags/search` - 搜索标签
- `PUT /api/tags/{id}` - 更新标签
- `DELETE /api/tags/{id}` - 删除标签

### 微信相关
- `GET /api/auth/wechat/auth-url` - 获取微信授权URL
- `POST /api/auth/wechat-login` - 微信登录
- `POST /api/wechat/qr-code` - 生成二维码
- `GET /api/wechat/scan-status/{scanId}` - 查询扫码状态

## 测试

### 1. 使用Postman测试
导入 `postman_collection.json` 到Postman中，包含所有API接口的测试用例。

### 2. 单元测试
```bash
mvn test
```

### 3. 集成测试
```bash
mvn verify
```

## 监控和日志

### 1. 应用日志
- 日志级别: INFO
- 日志文件: logs/application.log
- 日志轮转: 按天轮转，保留30天

### 2. 定时任务监控
- 任务执行日志
- 执行时间统计
- 失败告警

### 3. 性能监控
- API响应时间
- 数据库查询性能
- Redis缓存命中率

## 常见问题

### 1. 微信Access Token过期
- 系统会自动刷新Access Token
- 检查微信配置是否正确
- 查看定时任务日志

### 2. 提醒发送失败
- 检查提醒人信息是否正确
- 确认微信OpenID或手机号有效
- 查看短信/微信服务配置

### 3. 数据库连接失败
- 检查数据库服务是否启动
- 确认数据库连接配置
- 验证数据库用户权限

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者: [Your Name]
- 邮箱: [your.email@example.com]
- 项目地址: [https://github.com/your-username/user-auth-system]

## 更新日志

### v1.0.0 (2024-01-15)
- ✅ 基础用户认证功能
- ✅ 短信验证码登录
- ✅ 微信登录集成
- ✅ 定时提醒系统
- ✅ 提醒人管理
- ✅ 标签管理
- ✅ 定时任务功能
- ✅ Docker部署支持 