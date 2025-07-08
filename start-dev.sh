#!/bin/bash

echo "正在启动智能提醒系统前端开发服务器..."
echo

# 检查是否安装了Node.js
if ! command -v node &> /dev/null; then
    echo "错误: 未检测到Node.js，请先安装Node.js"
    echo "下载地址: https://nodejs.org/"
    exit 1
fi

# 显示Node.js版本
echo "Node.js版本: $(node --version)"
echo "npm版本: $(npm --version)"
echo

# 检查是否存在node_modules
if [ ! -d "node_modules" ]; then
    echo "检测到依赖未安装，正在安装依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "错误: 依赖安装失败"
        exit 1
    fi
    echo "依赖安装完成"
    echo
fi

echo "正在启动开发服务器..."
echo "服务器将在 http://localhost:3000 启动"
echo "请确保后端服务运行在 http://localhost:8080"
echo
echo "按 Ctrl+C 停止服务器"
echo

npm run dev 