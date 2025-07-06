# 数据库部署说明

本文档说明如何部署MySQL数据库并初始化用户认证系统。

## 📋 环境要求

- MySQL 8.0 或更高版本
- 支持utf8mb4字符集
- 至少100MB可用空间

## 🚀 快速部署

### 1. 安装MySQL

#### Windows
```bash
# 下载并安装MySQL 8.0
# 下载地址：https://dev.mysql.com/downloads/mysql/
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

#### macOS
```bash
# 使用Homebrew安装
brew install mysql
brew services start mysql
```

### 2. 配置MySQL

```bash
# 安全配置
sudo mysql_secure_installation

# 登录MySQL
mysql -u root -p
```

### 3. 创建数据库和用户

```sql
-- 登录MySQL后执行
CREATE DATABASE user_auth_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建专用用户（可选）
CREATE USER 'auth_user'@'localhost' IDENTIFIED BY 'auth_password';
GRANT ALL PRIVILEGES ON user_auth_system.* TO 'auth_user'@'localhost';
FLUSH PRIVILEGES;
```

### 4. 执行初始化脚本

```bash
# 方法1：命令行执行
mysql -u root -p user_auth_system < database/init.sql

# 方法2：在MySQL客户端中执行
mysql -u root -p
USE user_auth_system;
SOURCE database/init.sql;
```

## 🔧 配置应用

### 1. 修改数据库配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_auth_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root  # 或你创建的用户名
    password: 123456  # 你的MySQL密码
```

### 2. 使用MySQL配置文件

或者使用专门的MySQL配置文件：

```bash
# 启动应用时指定配置文件
java -jar app.jar --spring.profiles.active=mysql
```

## 📊 数据库结构

### 用户表 (users)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | 密码，BCrypt加密 |
| email | VARCHAR(100) | 邮箱，必填，唯一 |
| full_name | VARCHAR(100) | 用户全名 |
| phone_number | VARCHAR(20) | 手机号 |
| role | ENUM | 角色：USER/ADMIN |
| enabled | BOOLEAN | 是否启用 |
| account_non_expired | BOOLEAN | 账户是否未过期 |
| account_non_locked | BOOLEAN | 账户是否未锁定 |
| credentials_non_expired | BOOLEAN | 凭据是否未过期 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 审计日志表 (user_audit_log)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| action | VARCHAR(20) | 操作类型 |
| old_values | JSON | 更新前的值 |
| new_values | JSON | 更新后的值 |
| changed_at | TIMESTAMP | 操作时间 |
| changed_by | VARCHAR(50) | 操作人 |

## 🧪 测试数据

### 默认测试账号

| 用户名 | 密码 | 角色 | 状态 |
|--------|------|------|------|
| admin | 123456 | ADMIN | 启用 |
| zhangsan | 123456 | USER | 启用 |
| lisi | 123456 | USER | 启用 |
| wangwu | 123456 | USER | 启用 |
| zhaoliu | 123456 | USER | 启用 |
| testuser | 123456 | USER | 启用 |
| disabled_user | 123456 | USER | 禁用 |

## 🔍 验证部署

### 1. 检查数据库连接

```sql
-- 连接数据库
mysql -u root -p user_auth_system

-- 查看表结构
SHOW TABLES;
DESCRIBE users;

-- 查看测试数据
SELECT id, username, email, full_name, role, enabled FROM users;
```

### 2. 测试应用连接

```bash
# 启动应用
mvn spring-boot:run

# 或使用MySQL配置
mvn spring-boot:run -Dspring.profiles.active=mysql
```

### 3. 验证API接口

```bash
# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

## 📈 性能优化

### 1. 索引优化

数据库已包含以下索引：
- `idx_username` - 用户名索引
- `idx_email` - 邮箱索引
- `idx_phone_number` - 手机号索引
- `idx_role` - 角色索引
- `idx_enabled` - 启用状态索引
- `idx_created_at` - 创建时间索引

### 2. 连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 3. 查询优化

使用视图和存储过程：
- `active_users` - 活跃用户视图
- `user_stats` - 用户统计视图
- `GetUserStatistics()` - 用户统计存储过程
- `SearchUsers()` - 用户搜索存储过程

## 🔒 安全配置

### 1. 数据库安全

```sql
-- 限制用户权限
REVOKE ALL PRIVILEGES ON user_auth_system.* FROM 'auth_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON user_auth_system.* TO 'auth_user'@'localhost';

-- 禁用远程root登录
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');
FLUSH PRIVILEGES;
```

### 2. 网络安全

```bash
# 配置防火墙
sudo ufw allow 3306/tcp

# 或使用iptables
sudo iptables -A INPUT -p tcp --dport 3306 -s 192.168.1.0/24 -j ACCEPT
```

## 🛠️ 故障排除

### 常见问题

1. **连接被拒绝**
   ```bash
   # 检查MySQL服务状态
   sudo systemctl status mysql
   
   # 重启MySQL服务
   sudo systemctl restart mysql
   ```

2. **字符集问题**
   ```sql
   -- 检查字符集
   SHOW VARIABLES LIKE 'character_set%';
   
   -- 设置字符集
   SET NAMES utf8mb4;
   ```

3. **权限问题**
   ```sql
   -- 检查用户权限
   SHOW GRANTS FOR 'auth_user'@'localhost';
   
   -- 重新授权
   GRANT ALL PRIVILEGES ON user_auth_system.* TO 'auth_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

4. **时区问题**
   ```sql
   -- 检查时区
   SELECT @@global.time_zone, @@session.time_zone;
   
   -- 设置时区
   SET GLOBAL time_zone = '+08:00';
   SET time_zone = '+08:00';
   ```

### 日志查看

```bash
# MySQL错误日志
sudo tail -f /var/log/mysql/error.log

# 应用日志
tail -f logs/application.log
```

## 📚 相关文档

- [MySQL 8.0 官方文档](https://dev.mysql.com/doc/refman/8.0/en/)
- [Spring Boot 数据库配置](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-sql)
- [JPA/Hibernate 配置](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-jpa-and-spring-data)

## 🤝 贡献

如有问题或建议，请提交Issue或Pull Request。 