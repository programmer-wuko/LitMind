# 架构设计文档

## 系统架构

LitMind 采用前后端分离的单体架构设计：

```
┌─────────────┐
│  前端 (Next.js) │
│  Port: 3030   │
└──────┬───────┘
       │ HTTP/REST API
       │
┌──────▼──────────────────────────────┐
│      后端 (Spring Boot)              │
│      Port: 8080                      │
│  ┌────────────────────────────────┐ │
│  │  Controller Layer              │ │
│  │  - Auth, File, PDF, AI, Rec    │ │
│  └───────────┬────────────────────┘ │
│  ┌───────────▼────────────────────┐ │
│  │  Service Layer                 │ │
│  │  - Business Logic              │ │
│  └───────────┬────────────────────┘ │
│  ┌───────────▼────────────────────┐ │
│  │  Repository Layer               │ │
│  │  - Data Access                  │ │
│  └───────────┬────────────────────┘ │
└──────────────┼──────────────────────┘
               │
    ┌──────────┼──────────┐
    │          │          │
┌───▼───┐ ┌───▼───┐ ┌───▼───┐
│ MySQL │ │ Redis │ │ Kafka │
│ :3306 │ │ :6379 │ │ :9092 │
└───────┘ └───────┘ └───────┘
    │
┌───▼───┐
│ MinIO │
│ :9000 │
└───────┘
```

## 模块划分

### 后端模块

1. **认证模块** (`auth`)
   - 用户登录
   - JWT Token 生成与验证
   - Spring Security 配置

2. **文件管理模块** (`file`)
   - 文件上传/下载
   - 文件夹管理
   - MinIO 对象存储集成

3. **PDF处理模块** (`pdf`)
   - PDF 文本提取
   - PDF 分析结果管理
   - Kafka 异步分析任务

4. **AI服务模块** (`ai`)
   - LLM API 集成
   - PDF 智能分析
   - AI 问答功能

5. **推荐模块** (`recommend`)
   - 用户行为记录
   - 推荐算法
   - 推荐结果缓存

### 前端模块

1. **认证页面** (`/login`)
   - 登录表单
   - Token 管理

2. **主工作区** (`/dashboard`)
   - 文件列表
   - 文件夹树
   - 文件上传

3. **PDF分析页面** (`/pdf/[id]`)
   - PDF 阅读器
   - 智能分析面板
   - AI 问答面板

## 数据流

### PDF分析流程

```
用户上传PDF
    ↓
FileService.uploadFile()
    ↓
保存到MinIO + 数据库
    ↓
发送消息到Kafka (pdf-analysis topic)
    ↓
KafkaConsumerConfig.consumePdfAnalysis()
    ↓
PdfAnalysisService.analyzePdf()
    ↓
提取PDF文本 → 调用AI服务 → 保存分析结果
```

### AI问答流程

```
用户提问
    ↓
AiController.askQuestion()
    ↓
获取PDF文本 → 构建Prompt → 调用LLM API
    ↓
返回答案 → 保存问答记录
```

## 技术选型

### 后端
- **框架**: Spring Boot 2.7.18
- **数据库**: MySQL 8.0
- **缓存**: Redis 7
- **消息队列**: Kafka 3
- **对象存储**: MinIO (S3兼容)
- **认证**: JWT + Spring Security
- **PDF处理**: Apache PDFBox
- **HTTP客户端**: OkHttp

### 前端
- **框架**: Next.js 14 (App Router)
- **语言**: TypeScript
- **样式**: Tailwind CSS
- **状态管理**: Zustand
- **数据获取**: React Query
- **PDF渲染**: react-pdf
- **UI组件**: Lucide React Icons

## 安全设计

1. **认证**: JWT Token，24小时过期
2. **授权**: Spring Security 基于角色的访问控制
3. **数据隔离**: 所有操作基于 userId 进行数据隔离
4. **API安全**: CORS 配置，仅允许前端域名访问

## 性能优化

1. **缓存策略**: Redis 缓存推荐结果（1小时TTL）
2. **异步处理**: PDF分析通过Kafka异步处理
3. **文件存储**: MinIO 对象存储，支持大文件
4. **前端优化**: Next.js SSR/SSG，React Query 缓存

## 扩展性

虽然当前是单体架构，但代码采用模块化设计，便于后续拆分：

- 各模块独立分包（controller/service/repository）
- 统一的API响应格式
- 配置外部化
- 支持Docker容器化部署

未来可拆分为微服务：
- User Service
- File Service
- PDF Service
- AI Service
- Recommend Service

