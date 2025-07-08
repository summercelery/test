#!/bin/bash

# 极简部署脚本 - 最大兼容性版本
echo "======================================="
echo "       极简部署脚本                   "
echo "======================================="

# 基本颜色（简化版）
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 检查Docker Compose命令
check_compose() {
    if command -v docker-compose >/dev/null 2>&1; then
        COMPOSE_CMD="docker-compose"
    elif docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        echo -e "${RED}错误: 未找到 Docker Compose 命令${NC}"
        exit 1
    fi
    echo -e "${GREEN}使用命令: $COMPOSE_CMD${NC}"
}

# 启动MySQL
start_mysql() {
    echo -e "${YELLOW}检查MySQL容器...${NC}"
    if ! docker ps | grep mysql >/dev/null; then
        echo -e "${YELLOW}启动MySQL容器...${NC}"
        docker run -d --name mysql-app \
            -p 3306:3306 \
            -e MYSQL_ROOT_PASSWORD=123456 \
            -e MYSQL_DATABASE=user_auth_system \
            -e MYSQL_USER=auth_user \
            -e MYSQL_PASSWORD=auth_password \
            mysql:8.0 \
            --default-authentication-plugin=mysql_native_password \
            --character-set-server=utf8mb4 \
            --collation-server=utf8mb4_unicode_ci
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}MySQL启动成功${NC}"
            sleep 10
        else
            echo -e "${RED}MySQL启动失败${NC}"
            exit 1
        fi
    else
        echo -e "${GREEN}MySQL已运行${NC}"
    fi
}

# 启动Redis
start_redis() {
    echo -e "${YELLOW}检查Redis容器...${NC}"
    if ! docker ps | grep redis >/dev/null; then
        echo -e "${YELLOW}启动Redis容器...${NC}"
        docker run -d --name redis-app \
            -p 6379:6379 \
            redis:7-alpine redis-server --appendonly yes
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}Redis启动成功${NC}"
            sleep 5
        else
            echo -e "${RED}Redis启动失败${NC}"
            exit 1
        fi
    else
        echo -e "${GREEN}Redis已运行${NC}"
    fi
}

# 安装前端依赖
install_frontend() {
    echo -e "${YELLOW}安装前端依赖...${NC}"
    if [ ! -d "frontend/node_modules" ]; then
        cd frontend
        npm install
        if [ $? -ne 0 ]; then
            echo -e "${RED}前端依赖安装失败${NC}"
            cd ..
            exit 1
        fi
        cd ..
    fi
    echo -e "${GREEN}前端依赖已就绪${NC}"
}

# 部署应用
deploy_app() {
    echo -e "${BLUE}部署应用...${NC}"
    
    # 停止现有容器
    $COMPOSE_CMD -f docker-compose-simple.yml down
    
    # 启动新容器
    $COMPOSE_CMD -f docker-compose-simple.yml up -d --build
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}应用部署成功${NC}"
    else
        echo -e "${RED}应用部署失败${NC}"
        echo -e "${YELLOW}查看日志: $COMPOSE_CMD -f docker-compose-simple.yml logs${NC}"
        exit 1
    fi
}

# 检查服务状态
check_services() {
    echo -e "${BLUE}等待服务启动...${NC}"
    sleep 30
    
    echo -e "${BLUE}检查服务状态...${NC}"
    
    # 检查后端
    curl -f http://localhost:8080/api/user/hello >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ 后端服务正常${NC}"
    else
        echo -e "${YELLOW}⚠ 后端服务可能还在启动中${NC}"
    fi
    
    # 检查前端
    curl -f http://localhost/health >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ 前端服务正常${NC}"
    else
        echo -e "${YELLOW}⚠ 前端服务可能还在启动中${NC}"
    fi
}

# 显示结果
show_result() {
    echo -e "\n${GREEN}======================================="
    echo -e "         部署完成！                   "
    echo -e "=======================================${NC}"
    echo -e "${BLUE}访问地址:${NC}"
    echo -e "  前端: http://localhost"
    echo -e "  后端: http://localhost:8080/api"
    echo -e "\n${BLUE}管理命令:${NC}"
    echo -e "  查看日志: $COMPOSE_CMD -f docker-compose-simple.yml logs -f"
    echo -e "  停止服务: $COMPOSE_CMD -f docker-compose-simple.yml down"
    echo -e "  重启服务: $COMPOSE_CMD -f docker-compose-simple.yml restart"
}

# 主函数
main() {
    echo -e "${GREEN}开始部署...${NC}\n"
    
    # 设置脚本权限
    chmod +x *.sh
    
    # 执行部署步骤
    check_compose
    start_mysql
    start_redis
    install_frontend
    deploy_app
    check_services
    show_result
    
    echo -e "\n${GREEN}部署脚本执行完成！${NC}"
}

# 执行主函数
main 