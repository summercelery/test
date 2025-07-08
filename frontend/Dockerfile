# 前端本地编译文件部署Dockerfile
# 使用方式：本地先运行 npm run build，然后使用此Dockerfile部署

# 使用轻量级nginx镜像
FROM nginx:alpine


# 配置Alpine镜像源（可选，提高包安装速度）
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories

# 安装必要工具（可选）
RUN apk add --no-cache curl

# 删除默认nginx配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制nginx配置文件
# 注释掉这行，让docker-compose-simple.yml的volume挂载生效
# COPY nginx.conf /etc/nginx/conf.d/

# 复制本地编译好的静态文件
COPY dist/ /usr/share/nginx/html/

# 设置正确的权限
RUN chown -R nginx:nginx /usr/share/nginx/html
RUN chmod -R 755 /usr/share/nginx/html

# 创建日志目录
RUN mkdir -p /var/log/nginx

# 暴露端口
EXPOSE 80

# 添加健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/health || exit 1

# 启动nginx
CMD ["nginx", "-g", "daemon off;"] 