#!/bin/bash

echo "========================================"
echo "前端开发服务器启动脚本"
echo "========================================"
echo

echo "正在启动Python HTTP服务器..."
echo "服务器地址: http://localhost:8000"
echo "按 Ctrl+C 停止服务器"
echo

cd "$(dirname "$0")"
python3 -m http.server 8000 