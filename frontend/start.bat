@echo off
chcp 65001 >nul
echo 🚀 启动用户认证系统前端...

REM 检查Docker是否安装
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker未安装，请先安装Docker Desktop
    pause
    exit /b 1
)

REM 检查Docker Compose是否安装
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker Compose未安装，请先安装Docker Compose
    pause
    exit /b 1
)

REM 检查后端服务是否运行
echo 🔍 检查后端服务...
curl -s http://localhost:8080/api/auth/test >nul 2>&1
if errorlevel 1 (
    echo ⚠️  后端服务未运行，请先启动后端服务
    echo    后端服务应该运行在 http://localhost:8080
) else (
    echo ✅ 后端服务正在运行
)

REM 构建前端镜像
echo 🔨 构建前端Docker镜像...
docker build -t user-auth-frontend .

if errorlevel 1 (
    echo ❌ 前端镜像构建失败
    pause
    exit /b 1
) else (
    echo ✅ 前端镜像构建成功
)

REM 停止并删除旧容器
echo 🧹 清理旧容器...
docker stop frontend >nul 2>&1
docker rm frontend >nul 2>&1

REM 启动新容器
echo 🚀 启动前端容器...
docker run -d --name frontend -p 80:80 --restart unless-stopped user-auth-frontend

if errorlevel 1 (
    echo ❌ 前端服务启动失败
    pause
    exit /b 1
) else (
    echo ✅ 前端服务启动成功
    echo.
    echo 🌐 访问地址: http://localhost
    echo 📱 移动端也可以访问
    echo.
    echo 📋 使用说明:
    echo    1. 打开浏览器访问 http://localhost
    echo    2. 点击'注册'创建新账户
    echo    3. 使用新账户登录
    echo    4. 查看个人资料
    echo.
    echo 🔧 管理命令:
    echo    查看日志: docker logs frontend
    echo    停止服务: docker stop frontend
    echo    重启服务: docker restart frontend
    echo    删除服务: docker rm -f frontend
    echo.
    pause
) 