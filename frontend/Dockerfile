# 使用nginx官方镜像作为基础镜像
FROM nginx:alpine

# 复制前端文件到nginx默认目录
COPY index.html /usr/share/nginx/html/
COPY styles.css /usr/share/nginx/html/
COPY script.js /usr/share/nginx/html/

# 复制nginx配置文件
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 设置文件权限
RUN chmod 644 /usr/share/nginx/html/*

# 暴露80端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"] 