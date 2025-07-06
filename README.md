# 用户认证系统

这是一个基于Spring Boot的用户认证系统，提供用户注册、登录等功能。

## 技术栈

### 后端
- Spring Boot 2.7.14
- Spring Security
- Spring Data JPA
- Spring Data Redis
- JWT (JSON Web Token)
- H2 Database / MySQL
- Maven
- Lombok

### 前端
- HTML5 / CSS3 / JavaScript
- Bootstrap 5
- Font Awesome
- 响应式设计

### 部署
- Docker
- Docker Compose
- Nginx

## 功能特性

- ✅ 用户注册和登录
- ✅ JWT Token认证
- ✅ 用户信息管理
- ✅ 密码加密存储
- ✅ 全局异常处理
- ✅ 跨域支持
- ✅ Redis会话管理
- ✅ 会话统计和监控
- ✅ 邮箱注册支持
- ✅ 短信验证码登录
- ✅ 忘记密码重置
- ✅ 微信登录
- ✅ 美观的前端界面
- ✅ 响应式设计
- ✅ 实时消息提示

## 项目结构

```
src/main/java/com/example/
├── UserAuthApplication.java          # 主应用程序类
├── config/
│   └── SecurityConfig.java          # 安全配置
├── controller/
│   ├── AuthController.java          # 认证控制器
│   └── UserController.java          # 用户控制器
├── dto/
│   ├── ApiResponse.java             # 通用API响应
│   ├── AuthResponse.java            # 认证响应
│   ├── LoginRequest.java            # 登录请求
│   └── RegisterRequest.java         # 注册请求
├── entity/
│   └── User.java                    # 用户实体
├── exception/
│   └── GlobalExceptionHandler.java  # 全局异常处理器
├── repository/
│   └── UserRepository.java          # 用户数据访问层
├── security/
│   ├── CustomUserDetailsService.java # 自定义用户详情服务
│   ├── JwtRequestFilter.java        # JWT请求过滤器
│   └── JwtTokenUtil.java            # JWT工具类
└── service/
    ├── AuthService.java             # 认证服务
    └── UserService.java             # 用户服务
```

## 快速开始

### 1. 环境要求

- JDK 8+
- Maven 3.6+

### 2. 运行项目

```bash
# 克隆项目
git clone <repository-url>
cd user-auth-system

# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

### 3. 访问应用

- 应用地址: http://localhost:8080
- H2数据库控制台: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - 用户名: `sa`
  - 密码: (空)

## API接口

### 认证接口

#### 1. 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "fullName": "测试用户",
    "phoneNumber": "13800138000"
}
```

#### 2. 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "123456"
}
```

#### 3. 发送短信验证码
```
POST /api/auth/send-sms
Content-Type: application/json

{
    "phoneNumber": "13800138000",
    "smsType": "LOGIN"
}
```

#### 4. 短信验证码登录
```
POST /api/auth/sms-login
Content-Type: application/json

{
    "phoneNumber": "13800138000",
    "verificationCode": "123456"
}
```

#### 5. 重置密码
```
POST /api/auth/reset-password
Content-Type: application/json

{
    "phoneNumber": "13800138000",
    "verificationCode": "123456",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
}
```

#### 6. 认证测试
```
GET /api/auth/test
```

#### 7. 获取微信授权URL
```
GET /api/auth/wechat/auth-url?redirectUri={redirectUri}&state={state}
```

#### 8. 微信登录
```
POST /api/auth/wechat-login
Content-Type: application/json

{
    "code": "wechat_auth_code",
    "state": "state_value"
}
```

#### 9. 用户登出
```
POST /api/auth/logout
Authorization: Bearer <jwt-token>
```

### 用户接口

#### 1. 获取用户信息
```
GET /api/user/profile
Authorization: Bearer <jwt-token>
```

#### 2. 欢迎信息
```
GET /api/user/hello
Authorization: Bearer <jwt-token>
```

### 会话管理接口

#### 1. 获取会话统计
```
GET /api/session/stats
Authorization: Bearer <jwt-token>
```

#### 2. 延长会话
```
POST /api/session/extend
Authorization: Bearer <jwt-token>
```

#### 3. 强制下线其他设备
```
POST /api/session/logout-others
Authorization: Bearer <jwt-token>
```

## 响应格式

所有API都返回统一的响应格式：

```json
{
    "success": true,
    "message": "操作成功",
    "data": {
        // 具体数据
    }
}
```

## 安全说明

- 密码使用BCrypt加密存储
- 使用JWT进行无状态认证
- 所有用户接口都需要JWT令牌
- 支持CORS跨域请求

## 配置说明

主要配置在 `application.yml` 文件中：

- 数据库配置 (H2内存数据库)
- JWT密钥和过期时间
- 服务器端口
- 日志级别

## 开发说明

### 添加新功能

1. 在 `entity` 包中创建实体类
2. 在 `repository` 包中创建数据访问层
3. 在 `service` 包中创建业务逻辑
4. 在 `controller` 包中创建控制器
5. 在 `dto` 包中创建数据传输对象

### 测试

项目包含基本的单元测试，可以使用以下命令运行：

```bash
mvn test
```

## 许可证

MIT License 