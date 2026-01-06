# 使用Node.js官方镜像作为构建环境
FROM node:18-alpine AS build

# 设置工作目录
WORKDIR /app

# 复制package.json和package-lock.json文件
COPY package*.json ./

# 安装依赖
RUN npm install

# 复制源代码
COPY . .

# 构建项目
RUN npm run build

# 使用轻量级的Node.js镜像作为运行环境
FROM node:18-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制必要的文件
COPY --from=build /app/.next ./.next
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/package*.json ./
COPY --from=build /app/public ./public

# 暴露端口
EXPOSE 3030

# 启动应用
CMD ["npm", "start"]