# 多阶段构建 - 构建阶段
# 使用OpenJDK官方镜像，兼容性更好
FROM openjdk:8-jdk-slim AS build

# 设置工作目录
WORKDIR /app

# 配置APT使用国内镜像源
RUN echo "deb https://mirrors.ustc.edu.cn/debian/ bullseye main" > /etc/apt/sources.list && \
    echo "deb https://mirrors.ustc.edu.cn/debian/ bullseye-updates main" >> /etc/apt/sources.list && \
    echo "deb https://mirrors.ustc.edu.cn/debian-security bullseye-security main" >> /etc/apt/sources.list

# 安装Maven - 添加重试机制和错误处理
RUN apt-get update --fix-missing && \
    apt-get install -y --no-install-recommends \
    maven \
    wget \
    ca-certificates \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# 复制pom.xml文件
COPY pom.xml .

# 配置Maven使用国内镜像源
RUN mkdir -p ~/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > ~/.m2/settings.xml && \
    echo '<settings>' >> ~/.m2/settings.xml && \
    echo '  <mirrors>' >> ~/.m2/settings.xml && \
    echo '    <mirror>' >> ~/.m2/settings.xml && \
    echo '      <id>aliyun</id>' >> ~/.m2/settings.xml && \
    echo '      <mirrorOf>central</mirrorOf>' >> ~/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/central</url>' >> ~/.m2/settings.xml && \
    echo '    </mirror>' >> ~/.m2/settings.xml && \
    echo '  </mirrors>' >> ~/.m2/settings.xml && \
    echo '</settings>' >> ~/.m2/settings.xml

# 下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /app

# 配置APT使用国内镜像源
RUN echo "deb https://mirrors.ustc.edu.cn/debian/ bullseye main" > /etc/apt/sources.list && \
    echo "deb https://mirrors.ustc.edu.cn/debian/ bullseye-updates main" >> /etc/apt/sources.list && \
    echo "deb https://mirrors.ustc.edu.cn/debian-security bullseye-security main" >> /etc/apt/sources.list

# 安装必要的工具
RUN apt-get update --fix-missing && \
    apt-get install -y --no-install-recommends \
    curl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# 创建非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app/logs

# 从构建阶段复制jar文件
COPY --from=build /app/target/*.jar app.jar

# 设置文件权限
RUN chown -R appuser:appuser /app
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/auth/test || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"] 