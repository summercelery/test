#!/bin/bash

# 快速部署脚本 - 简化版本
echo "======================================="
echo "       快速部署脚本（简化版本）       "
echo "======================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查并设置Docker Compose命令
check_docker_compose() {
    if command -v docker-compose >/dev/null 2>&1; then
        DOCKER_COMPOSE_CMD="docker-compose"
        echo -e "${GREEN}使用 docker-compose 命令${NC}"
    elif docker compose version >/dev/null 2>&1; then
        DOCKER_COMPOSE_CMD="docker compose"
        echo -e "${GREEN}使用 docker compose 命令${NC}"
    else
        echo -e "${RED}错误: 未找到 docker-compose 或 docker compose 命令${NC}"
        echo -e "${YELLOW}请确保 Docker 和 Docker Compose 已正确安装${NC}"
        exit 1
    fi
}

# 检查Docker镜像源
check_docker_mirror() {
    echo -e "${BLUE}检查Docker镜像源...${NC}"
    
    # 测试拉取小镜像
    if timeout 30 docker pull hello-world:latest >/dev/null 2>&1; then
        echo -e "${GREEN}✓ Docker镜像拉取正常${NC}"
        docker rmi hello-world:latest >/dev/null 2>&1
        return 0
    else
        echo -e "${RED}✗ Docker镜像拉取失败${NC}"
        echo -e "${YELLOW}建议配置Docker镜像源以加速拉取${NC}"
        echo -e "${BLUE}可以运行: ./configure-docker-mirror.sh${NC}"
        
        read -p "是否继续部署? (y/n): " continue_deploy
        if [ "$continue_deploy" != "y" ] && [ "$continue_deploy" != "Y" ]; then
            echo -e "${YELLOW}部署已取消${NC}"
            exit 1
        fi
        return 1
    fi
}

# 预拉取必要镜像
pre_pull_images() {
    echo -e "${BLUE}预拉取必要镜像...${NC}"
    
    # 关键镜像列表（使用字符串，避免数组兼容性问题）
    CRITICAL_IMAGES="openjdk:8-jdk-slim openjdk:8-jre-slim node:16-alpine nginx:alpine mysql:8.0 redis:7-alpine"
    
    failed_images=""
    
    for image in $CRITICAL_IMAGES; do
        echo -e "${YELLOW}拉取 $image...${NC}"
        if timeout 60 docker pull "$image" >/dev/null 2>&1; then
            echo -e "${GREEN}✓ $image 拉取成功${NC}"
        else
            echo -e "${RED}✗ $image 拉取失败${NC}"
            if [ -z "$failed_images" ]; then
                failed_images="$image"
            else
                failed_images="$failed_images $image"
            fi
        fi
    done
    
    if [ -n "$failed_images" ]; then
        echo -e "${RED}以下镜像拉取失败:${NC}"
        for image in $failed_images; do
            echo -e "  - $image"
        done
        echo -e "${YELLOW}建议配置镜像源: ./configure-docker-mirror.sh${NC}"
        
        read -p "是否继续部署? (y/n): " continue_deploy
        if [ "$continue_deploy" != "y" ] && [ "$continue_deploy" != "Y" ]; then
            echo -e "${YELLOW}部署已取消${NC}"
            exit 1
        fi
    fi
}

# 设置脚本权限
chmod +x deploy.sh configure-docker-mirror.sh switch-jdk-version.sh

echo -e "${BLUE}正在执行快速部署...${NC}\n"

# 检查Docker Compose
check_docker_compose

# 检查Docker镜像源
check_docker_mirror
mirror_ok=$?

# 如果镜像源有问题，预拉取镜像
if [ $mirror_ok -ne 0 ]; then
    pre_pull_images
fi

# 检查MySQL和Redis
echo -e "${YELLOW}检查MySQL和Redis容器...${NC}"
MYSQL_RUNNING=$(docker ps --filter "ancestor=mysql" --format "{{.Names}}" | head -1)
REDIS_RUNNING=$(docker ps --filter "ancestor=redis" --format "{{.Names}}" | head -1)

if [ -z "$MYSQL_RUNNING" ]; then
    echo -e "${YELLOW}启动MySQL容器...${NC}"
    if ! docker run -d --name mysql-app \
        -p 3306:3306 \
        -e MYSQL_ROOT_PASSWORD=123456 \
        -e MYSQL_DATABASE=user_auth_system \
        -e MYSQL_USER=auth_user \
        -e MYSQL_PASSWORD=auth_password \
        mysql:8.0 \
        --default-authentication-plugin=mysql_native_password \
        --character-set-server=utf8mb4 \
        --collation-server=utf8mb4_unicode_ci; then
        echo -e "${RED}✗ MySQL容器启动失败${NC}"
        echo -e "${YELLOW}请检查网络连接或配置镜像源${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}MySQL容器启动完成${NC}"
    sleep 10
else
    echo -e "${GREEN}MySQL容器已运行: $MYSQL_RUNNING${NC}"
fi

if [ -z "$REDIS_RUNNING" ]; then
    echo -e "${YELLOW}启动Redis容器...${NC}"
    if ! docker run -d --name redis-app -p 6379:6379 redis:7-alpine redis-server --appendonly yes; then
        echo -e "${RED}✗ Redis容器启动失败${NC}"
        echo -e "${YELLOW}请检查网络连接或配置镜像源${NC}"
        exit 1
    fi
    echo -e "${GREEN}Redis容器启动完成${NC}"
    sleep 5
else
    echo -e "${GREEN}Redis容器已运行: $REDIS_RUNNING${NC}"
fi

# 安装前端依赖
echo -e "${YELLOW}安装前端依赖...${NC}"
if [ ! -d "frontend/node_modules" ]; then
    cd frontend
    if ! npm install; then
        echo -e "${RED}✗ 前端依赖安装失败${NC}"
        echo -e "${YELLOW}请检查网络连接或配置npm镜像源${NC}"
        cd ..
        exit 1
    fi
    cd ..
fi

# 使用简化部署
echo -e "${BLUE}使用简化配置部署应用...${NC}"
$DOCKER_COMPOSE_CMD -f docker-compose-simple.yml down

# 构建和启动容器，增加错误处理
echo -e "${YELLOW}构建并启动容器...${NC}"
if ! $DOCKER_COMPOSE_CMD -f docker-compose-simple.yml up -d --build; then
    echo -e "${RED}✗ 容器构建失败${NC}"
    echo -e "${YELLOW}可能的解决方案:${NC}"
    echo -e "1. 配置Docker镜像源: ./configure-docker-mirror.sh"
    echo -e "2. 检查JDK版本: ./switch-jdk-version.sh"
    echo -e "3. 查看构建日志: $DOCKER_COMPOSE_CMD -f docker-compose-simple.yml logs"
    exit 1
fi

# 等待服务启动
echo -e "${YELLOW}等待服务启动...${NC}"
sleep 30

# 检查服务状态
echo -e "${BLUE}检查服务状态...${NC}"
if curl -f http://localhost:8080/api/user/hello >/dev/null 2>&1; then
    echo -e "${GREEN}✓ 后端服务运行正常${NC}"
else
    echo -e "${RED}✗ 后端服务可能还在启动中，请稍后检查${NC}"
fi

if curl -f http://localhost/health >/dev/null 2>&1; then
    echo -e "${GREEN}✓ 前端服务运行正常${NC}"
else
    echo -e "${RED}✗ 前端服务可能还在启动中，请稍后检查${NC}"
fi

echo -e "\n${GREEN}======================================="
echo -e "         部署完成！                   "
echo -e "=======================================${NC}"
echo -e "${BLUE}访问地址:${NC}"
echo -e "  🌐 前端: http://localhost"
echo -e "  🔧 后端API: http://localhost:8080/api"
echo -e "\n${BLUE}管理命令:${NC}"
echo -e "  📋 查看日志: $DOCKER_COMPOSE_CMD -f docker-compose-simple.yml logs -f"
echo -e "  ⏹️  停止服务: $DOCKER_COMPOSE_CMD -f docker-compose-simple.yml down"
echo -e "  🔄 重启服务: $DOCKER_COMPOSE_CMD -f docker-compose-simple.yml restart"
echo -e "\n${BLUE}数据库信息:${NC}"
echo -e "  🗄️  MySQL: localhost:3306"
echo -e "  📦 Redis: localhost:6379"
echo -e "\n${BLUE}工具脚本:${NC}"
echo -e "  🔧 配置镜像源: ./configure-docker-mirror.sh"
echo -e "  ☕ 切换JDK版本: ./switch-jdk-version.sh"
echo -e "  🧪 测试Docker: ./test-docker-compose.sh"
echo -e "\n${YELLOW}如果服务未正常启动，请等待1-2分钟后访问${NC}" 