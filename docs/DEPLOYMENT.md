# 部署指南

## 本地开发环境部署

### 1. 前置要求

- Node.js 18+
- Java 8+
- Maven 3.8+
- Docker & Docker Compose

### 2. 启动基础设施

```bash
docker-compose up -d
```

这将启动：
- MySQL (端口: 3306)
- Redis (端口: 6379)
- Kafka + Zookeeper (端口: 9092)
- MinIO (端口: 9000, 控制台: 9001)

### 3. 配置 MinIO

1. 访问 http://localhost:9001
2. 登录（用户名: `minioadmin`, 密码: `minioadmin123`）
3. 创建存储桶：`litmind-files`

### 4. 配置后端

编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
ai:
  api-key: your-openai-api-key  # 替换为你的 OpenAI API Key
```

### 5. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务运行在: http://localhost:8080

### 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务运行在: http://localhost:3030

### 7. 登录

- 用户名: `admin`
- 密码: `admin123`

## 生产环境部署

### Docker 部署

1. 构建后端镜像：

```bash
cd backend
mvn clean package
docker build -t litmind-backend:latest .
```

2. 构建前端镜像：

```bash
cd frontend
npm run build
docker build -t litmind-frontend:latest .
```

3. 使用 Docker Compose 部署：

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 环境变量配置

生产环境需要配置以下环境变量：

```bash
# 数据库
DB_URL=jdbc:mysql://mysql:3306/litmind
DB_USERNAME=litmind
DB_PASSWORD=your-password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# MinIO
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET=litmind-files

# JWT
JWT_SECRET=your-jwt-secret-key

# AI服务
AI_PROVIDER=openai
AI_API_KEY=your-openai-api-key
AI_BASE_URL=https://api.openai.com/v1
AI_MODEL=gpt-4-turbo-preview
```

## 常见问题

### 1. 数据库连接失败

检查 MySQL 容器是否正常运行：
```bash
docker ps | grep mysql
```

### 2. MinIO 连接失败

确保 MinIO 容器正常运行，并且已创建存储桶。

### 3. Kafka 消息未消费

检查 Kafka 和 Zookeeper 容器是否正常运行。

### 4. AI 服务调用失败

检查 API Key 是否正确配置，以及网络连接是否正常。

