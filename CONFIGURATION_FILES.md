# Spring Boot 配置文件说明

## 📁 配置文件结构

项目中有三个不同的配置文件，这是Spring Boot的**Profile配置分离**策略，用于不同环境的配置管理：

```
src/main/resources/
├── application.yml          # 默认配置（开发环境 - H2数据库）
├── application-mysql.yml    # MySQL配置（生产环境）
└── application-docker.yml   # Docker配置（容器化部署）
```

## 🎯 各配置文件用途

### 1. `application.yml` - 默认配置
**用途**：开发环境，使用H2内存数据库
**特点**：
- 使用H2内存数据库，无需额外安装
- 适合快速开发和测试
- 数据不持久化，重启后丢失

**主要配置**：
```yaml
# 数据库配置
datasource:
  url: jdbc:h2:mem:testdb
  driver-class-name: org.h2.Driver
  username: sa
  password: 

# JPA配置
jpa:
  hibernate:
    ddl-auto: create-drop  # 每次启动重新创建表
```

### 2. `application-mysql.yml` - MySQL配置
**用途**：生产环境，使用MySQL数据库
**特点**：
- 使用MySQL数据库，数据持久化
- 适合生产环境部署
- 需要预先安装MySQL

**主要配置**：
```yaml
# 数据库配置
datasource:
  url: jdbc:mysql://localhost:3306/user_auth_system
  driver-class-name: com.mysql.cj.jdbc.Driver
  username: root
  password: 123456

# JPA配置
jpa:
  hibernate:
    ddl-auto: validate  # 验证表结构，不自动创建
```

### 3. `application-docker.yml` - Docker配置
**用途**：容器化部署环境
**特点**：
- 适配Docker容器网络
- 使用容器服务名作为主机名
- 适合Docker Compose部署

**主要配置**：
```yaml
# 数据库配置（Docker环境）
datasource:
  url: jdbc:mysql://mysql:3306/user_auth_system  # 使用容器服务名
  username: auth_user
  password: auth_password

# Redis配置（Docker环境）
redis:
  host: redis  # 使用容器服务名
```

## 🚀 如何使用不同配置

### 1. 使用默认配置（H2）
```bash
# 直接启动，使用application.yml
mvn spring-boot:run
```

### 2. 使用MySQL配置
```bash
# 指定使用mysql profile
mvn spring-boot:run -Dspring.profiles.active=mysql
```

### 3. 使用Docker配置
```bash
# 指定使用docker profile
mvn spring-boot:run -Dspring.profiles.active=docker
```

### 4. 打包时指定配置
```bash
# 打包时指定profile
mvn clean package -Dspring.profiles.active=mysql
```

## 🔧 配置文件加载顺序

Spring Boot按以下顺序加载配置：

1. **application.yml** - 基础配置
2. **application-{profile}.yml** - 特定环境配置（覆盖基础配置）

例如，使用`mysql` profile时：
- 先加载 `application.yml`
- 再加载 `application-mysql.yml`（覆盖相同配置）

## 📊 配置对比表

| 配置项 | application.yml | application-mysql.yml | application-docker.yml |
|--------|----------------|----------------------|----------------------|
| 数据库 | H2内存数据库 | MySQL本地 | MySQL容器 |
| 数据库URL | jdbc:h2:mem:testdb | localhost:3306 | mysql:3306 |
| 用户名 | sa | root | auth_user |
| 密码 | 空 | 123456 | auth_password |
| DDL模式 | create-drop | validate | validate |
| Redis主机 | localhost | localhost | redis |
| 适用环境 | 开发测试 | 生产环境 | 容器部署 |

## 🛠️ 环境切换示例

### 开发环境
```bash
# 使用H2数据库，快速开发
mvn spring-boot:run
```

### 本地MySQL测试
```bash
# 使用本地MySQL数据库
mvn spring-boot:run -Dspring.profiles.active=mysql
```

### Docker部署
```bash
# 使用Docker Compose启动所有服务
docker-compose up -d
```

## 🔍 配置文件验证

### 检查当前使用的配置
```bash
# 启动时查看日志，确认使用的profile
mvn spring-boot:run -Dspring.profiles.active=mysql
```

### 查看配置信息
```bash
# 访问配置信息端点（如果启用）
curl http://localhost:8080/actuator/env
```

## 📝 最佳实践

### 1. 配置分离原则
- **基础配置**：放在`application.yml`
- **环境特定配置**：放在`application-{profile}.yml`
- **敏感信息**：使用环境变量或外部配置

### 2. 安全考虑
```yaml
# 生产环境建议使用环境变量
datasource:
  username: ${DB_USERNAME:root}
  password: ${DB_PASSWORD:}
```

### 3. 配置验证
```yaml
# 添加配置验证
spring:
  config:
    import: optional:file:./config/
```

## 🚨 注意事项

### 1. 数据库依赖
- **H2**：无需额外安装，内置支持
- **MySQL**：需要安装MySQL 8.0+
- **Docker**：需要安装Docker和Docker Compose

### 2. 数据持久化
- **H2**：数据不持久化，重启丢失
- **MySQL**：数据持久化，需要备份
- **Docker**：数据持久化，使用卷挂载

### 3. 网络配置
- **本地**：使用localhost
- **Docker**：使用容器服务名
- **生产**：使用实际IP或域名

## 📚 相关文档

- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-profiles)
- [外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
- [数据库配置](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-sql) 