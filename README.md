# LitMind - 科研文献智能分析平台

一个基于 AI 的科研文献智能分析平台，支持 PDF 阅读、智能分析、个性化推荐和 AI 问答。

## 技术栈

### 前端
- **框架**: Next.js 14 (App Router)
- **语言**: TypeScript
- **样式**: Tailwind CSS
- **状态管理**: React Query + Zustand
- **PDF 渲染**: react-pdf

### 后端
- **框架**: Spring Boot 2.7.18
- **语言**: Java 8
- **数据库**: MySQL 8.0
- **缓存**: Redis 7
- **消息队列**: Kafka 3
- **文件存储**: MinIO (S3 兼容)
- **认证**: JWT + Spring Security

## 功能特性

- ✅ 用户登录认证
- ✅ 文件与文件夹管理（树形结构）
- ✅ PDF 上传与查看
- ✅ 智能分析（自动生成研究背景、核心内容、实验结果等）
- ✅ AI 问答助手（基于论文内容的智能问答）
- ✅ 个性化推荐（基于用户行为的文献推荐）

## 快速开始

### 前置要求

- Node.js 18+
- Java 8+
- Maven 3.8+
- Docker & Docker Compose

### 1. 启动基础设施

```bash
docker-compose up -d
```

这将启动：
- MySQL (端口: 3306)
- Redis (端口: 6379)
- Kafka (端口: 9092)
- MinIO (端口: 9000, 控制台: 9001)

### 2. 配置 MinIO

访问 http://localhost:9001 登录 MinIO 控制台：
- 用户名: `minioadmin`
- 密码: `minioadmin123`

创建存储桶：`litmind-files`

### 3. 启动后端服务

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将运行在: http://localhost:8080

### 4. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

前端服务将运行在: http://localhost:3030

### 5. 初始化数据库

数据库会在 Docker 启动时自动初始化，或手动执行：

```bash
mysql -u litmind -p litmind < sql/init.sql
```

## 环境配置

### 后端配置

编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/litmind
    username: litmind
    password: litmind123
  
  redis:
    host: localhost
    port: 6379

kafka:
  bootstrap-servers: localhost:9092

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin123
  bucket: litmind-files

ai:
  api-key: your-llm-api-key  # 配置你的 LLM API Key
  provider: openai  # 或 anthropic, local 等
```

### 前端配置

复制 `frontend/.env.local.example` 为 `frontend/.env.local`：

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## 项目结构

```
LitMind/
├── frontend/              # Next.js 前端项目
│   ├── app/              # App Router 页面
│   ├── components/       # React 组件
│   ├── lib/             # 工具库
│   └── types/           # TypeScript 类型
├── backend/             # Spring Boot 后端项目
│   ├── src/main/java/com/litmind/
│   │   ├── controller/  # REST 控制器
│   │   ├── service/     # 业务逻辑
│   │   ├── repository/  # 数据访问
│   │   └── model/       # 实体类
│   └── src/main/resources/
│       └── application.yml
├── sql/                 # 数据库脚本
├── docs/               # 项目文档
└── docker-compose.yml  # 基础设施编排
```

## API 文档

启动后端服务后，访问 Swagger UI：
http://localhost:8080/swagger-ui.html

## 开发指南

### 代码规范

- 前端：ESLint + Prettier
- 后端：Google Java Style Guide
- 提交信息：Conventional Commits

### 测试

```bash
# 前端测试
cd frontend
npm test

# 后端测试
cd backend
mvn test
```

## 部署

### 生产环境配置

1. 修改 `application-prod.yml` 中的数据库、Redis 等连接信息
2. 配置环境变量（API Keys、密钥等）
3. 构建前端：`npm run build`
4. 构建后端：`mvn clean package`
5. 使用 Docker 部署或直接运行 JAR 文件

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

