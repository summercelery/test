@echo off
echo 正在启动智能提醒系统前端开发服务器...
echo.

REM 检查是否安装了Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到Node.js，请先安装Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)

REM 检查是否存在node_modules
if not exist "node_modules" (
    echo 检测到依赖未安装，正在安装依赖...
    npm install
    if %errorlevel% neq 0 (
        echo 错误: 依赖安装失败
        pause
        exit /b 1
    )
)

echo 正在启动开发服务器...
echo 服务器将在 http://localhost:3000 启动
echo 请确保后端服务运行在 http://localhost:8080
echo.
echo 按 Ctrl+C 停止服务器
echo.

npm run dev

pause 