# Redis 用户会话管理集成

本项目已集成Redis用于用户会话管理，提供更安全、高效的认证机制。

## 🚀 功能特性

### 🔐 **用户会话管理**
- **会话存储**：用户登录后，会话信息自动存储到Redis
- **会话验证**：每次请求都会验证Redis中的会话有效性
- **会话过期**：支持自动过期机制（默认24小时）
- **会话延长**：支持延长会话时间

### 📊 **会话信息记录**
- **用户基本信息**：用户ID、用户名、角色等
- **登录信息**：登录时间、最后访问时间
- **设备信息**：IP地址、User-Agent
- **Token管理**：JWT Token与Redis会话关联

### 🛡️ **安全特性**
- **双重验证**：JWT Token + Redis会话验证
- **会话隔离**：支持强制下线其他设备
- **会话统计**：提供详细的会话统计信息
- **安全登出**：登出时清除Redis中的会话数据

## 📁 新增文件

### 配置类
- `RedisConfig.java` - Redis配置类
- `UserSession.java` - 用户会话信息DTO

### 服务类
- `RedisService.java` - Redis操作服务
- `SessionController.java` - 会话管理控制器

### 修改文件
- `AuthService.java` - 登录时存储会话到Redis
- `JwtRequestFilter.java` - 从Redis验证会话
- `AuthController.java` - 添加登出接口
- `application.yml` - Redis配置
- `docker-compose.yml` - 添加Redis服务

## 🔧 配置说明

### Redis配置 (application.yml)
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

### Docker环境变量
```yaml
environment:
  - SPRING_REDIS_HOST=redis
  - SPRING_REDIS_PORT=6379
```

## 📡 API接口

### 认证接口
- `POST /api/auth/login` - 用户登录（自动存储会话到Redis）
- `POST /api/auth/logout` - 用户登出（清除Redis会话）

### 会话管理接口
- `GET /api/session/current` - 获取当前会话信息
- `GET /api/session/stats` - 获取会话统计信息
- `POST /api/session/extend` - 延长会话时间
- `POST /api/session/logout-others` - 强制下线其他设备
- `PUT /api/session/update` - 更新会话信息

## 🚀 部署说明

### 使用Docker Compose（推荐）
```bash
# 启动完整应用栈（包含Redis）
docker-compose up -d
```

### 手动部署
1. **启动Redis服务**
   ```bash
   # 使用Docker
   docker run -d --name redis -p 6379:6379 redis:7-alpine
   
   # 或使用本地Redis
   redis-server
   ```

2. **启动后端应用**
   ```bash
   mvn spring-boot:run
   ```

## 🔍 使用示例

### 登录流程
1. 用户提交登录请求
2. 验证用户名密码
3. 生成JWT Token
4. 创建用户会话对象
5. 存储会话到Redis
6. 返回Token给前端

### 请求验证流程
1. 前端携带Token发送请求
2. JWT Filter提取Token
3. 验证Token在Redis中是否存在
4. 获取用户会话信息
5. 更新最后访问时间
6. 延长会话过期时间
7. 设置Spring Security上下文

### 登出流程
1. 前端调用登出接口
2. 从请求头提取Token
3. 删除Redis中的会话数据
4. 清除前端本地存储
5. 跳转到登录页面

## 📊 会话数据结构

### Redis Key格式
- 会话信息：`user:session:{token}`
- Token映射：`user:token:{username}`

### 会话信息字段
```json
{
  "userId": "1",
  "username": "user123",
  "phoneNumber": "13812345678",
  "email": "user@example.com",
  "fullName": "张三",
  "role": "USER",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "loginTime": "2024-01-01T10:00:00",
  "lastAccessTime": "2024-01-01T10:30:00",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0..."
}
```

## 🔧 监控和管理

### 查看Redis数据
```bash
# 连接Redis
redis-cli

# 查看所有会话
KEYS user:session:*

# 查看特定会话
GET user:session:{token}

# 查看会话过期时间
TTL user:session:{token}
```

### 会话统计
```bash
# 获取当前在线用户数
KEYS user:session:* | wc -l

# 查看会话过期时间分布
TTL user:session:{token}
```

## 🛠️ 故障排除

### 常见问题

1. **Redis连接失败**
   - 检查Redis服务是否启动
   - 验证连接配置是否正确
   - 确认网络连通性

2. **会话验证失败**
   - 检查Token是否在Redis中存在
   - 验证会话是否已过期
   - 确认Redis序列化配置

3. **性能问题**
   - 调整Redis连接池配置
   - 优化会话过期时间
   - 监控Redis内存使用

### 日志查看
```bash
# 查看应用日志
docker-compose logs backend

# 查看Redis日志
docker-compose logs redis
```

## 🔒 安全建议

1. **Redis安全配置**
   - 设置Redis密码
   - 限制网络访问
   - 启用SSL/TLS

2. **会话管理**
   - 合理设置会话过期时间
   - 实现会话并发控制
   - 定期清理过期会话

3. **监控告警**
   - 监控Redis连接状态
   - 设置会话异常告警
   - 记录安全事件日志

## 📈 性能优化

1. **Redis优化**
   - 使用Redis集群
   - 配置持久化策略
   - 优化内存使用

2. **应用优化**
   - 使用连接池
   - 实现缓存策略
   - 异步处理非关键操作

3. **监控指标**
   - 会话创建/删除速率
   - Redis响应时间
   - 内存使用情况 