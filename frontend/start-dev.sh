#!/bin/bash

echo "========================================"
echo "智能提醒管理系统 - 前端开发服务器"
echo "========================================"
echo

# 检查 Node.js 是否安装
if ! command -v node &> /dev/null; then
    echo "[错误] 未检测到 Node.js，请先安装 Node.js 16.0.0 或更高版本"
    echo "下载地址: https://nodejs.org/"
    exit 1
fi

# 显示 Node.js 版本
echo "[信息] Node.js 版本:"
node --version
echo

# 检查是否存在 node_modules
if [ ! -d "node_modules" ]; then
    echo "[信息] 检测到首次运行，正在安装依赖..."
    echo
    npm install
    if [ $? -ne 0 ]; then
        echo "[错误] 依赖安装失败"
        exit 1
    fi
    echo
    echo "[成功] 依赖安装完成"
    echo
fi

# 启动开发服务器
echo "[信息] 正在启动开发服务器..."
echo "[信息] 服务器地址: http://localhost:5173"
echo "[信息] 按 Ctrl+C 停止服务器"
echo

npm run dev 