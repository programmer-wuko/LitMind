# 本地启动指南

## 方式一：简化启动（推荐，无需Docker）

### 前置要求
- ✅ JDK 8
- ✅ Maven 3.6+
- ✅ Node.js 18+
- ✅ IDEA（或其他Java IDE）
- ✅ OpenAI API Key（用于AI功能）

### 步骤1：配置后端

#### 1.1 导入项目到IDEA
1. 打开IDEA
2. File → Open → 选择 `LitMind` 目录
3. 等待Maven依赖自动下载（或手动执行 `mvn clean install`）

#### 1.2 配置运行参数
1. 找到 `backend/src/main/java/com/litmind/LitMindApplication.java`
2. 右键 → `Run 'LitMindApplication'` → `Edit Configurations...`
3. 在 `Active profiles` 填入：`local`
   - 或者添加环境变量：`SPRING_PROFILES_ACTIVE=local`

#### 1.3 配置AI API Key
编辑 `backend/src/main/resources/application-local.yml`：

```yaml
ai:
  api-key: sk-your-openai-api-key-here  # 替换为你的实际API Key
```

或者设置环境变量：
- Windows: `set AI_API_KEY=sk-your-key`
- Linux/Mac: `export AI_API_KEY=sk-your-key`

#### 1.4 启动后端
- 点击IDEA的运行按钮（绿色三角形）
- 看到 `Started LitMindApplication` 表示启动成功
- 后端运行在：http://localhost:8080/api

### 步骤2：配置前端

#### 2.1 安装依赖
```bash
cd frontend
npm install
```

#### 2.2 配置环境变量
复制并编辑环境变量文件：
```bash
# Windows
copy .env.local.example .env.local

# Linux/Mac
cp .env.local.example .env.local
```

编辑 `.env.local`，确保API地址正确：
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

#### 2.3 启动前端
```bash
npm run dev
```

前端运行在：http://localhost:3030

### 步骤3：访问应用

1. 打开浏览器访问：http://localhost:3030
2. 使用默认账号登录：
   - 用户名：`admin`
   - 密码：`admin123`

---

## 方式二：完整启动（需要Docker）

### 前置要求
- ✅ 方式一的所有要求
- ✅ Docker Desktop

### 步骤1：启动基础设施

```bash
# 在项目根目录执行
docker-compose up -d
```

这将启动：
- MySQL (端口: 3306)
- Redis (端口: 6379)
- Kafka + Zookeeper (端口: 9092)
- MinIO (端口: 9000, 控制台: 9001)

### 步骤2：配置MinIO

1. 访问 http://localhost:9001
2. 登录：
   - 用户名: `minioadmin`
   - 密码: `minioadmin123`
3. 创建存储桶：`litmind-files`

### 步骤3：配置后端

#### 3.1 配置运行参数
在IDEA中设置 `Active profiles: dev`（或不设置，默认使用dev）

#### 3.2 配置AI API Key
编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
ai:
  api-key: sk-your-openai-api-key-here
```

#### 3.3 启动后端
- 点击IDEA的运行按钮
- 等待启动完成

### 步骤4：启动前端

同方式一的步骤2

---

## 验证启动是否成功

### 后端验证
1. 访问：http://localhost:8080/api/swagger-ui.html
   - 应该能看到Swagger API文档页面

2. 访问：http://localhost:8080/api/h2-console（仅local模式）
   - JDBC URL: `jdbc:h2:mem:litmind`
   - 用户名: `sa`
   - 密码: （留空）

### 前端验证
1. 访问：http://localhost:3030
2. 应该能看到登录页面

---

## 常见问题

### Q1: 后端启动失败，提示端口被占用
**解决方案**：
- 修改 `backend/src/main/resources/application.yml` 中的 `server.port`
- 或关闭占用8080端口的其他程序

### Q2: 前端无法连接后端
**解决方案**：
- 检查 `.env.local` 中的 `NEXT_PUBLIC_API_URL` 是否正确
- 确认后端已成功启动
- 检查浏览器控制台是否有CORS错误

### Q3: 登录失败
**解决方案**：
- 确认数据库已初始化（local模式会自动创建表）
- 检查默认账号密码：`admin` / `admin123`
- 查看后端日志是否有错误信息

### Q4: PDF分析功能不工作
**解决方案**：
- 检查AI API Key是否正确配置
- 确认网络可以访问OpenAI API
- 查看后端日志中的错误信息

### Q5: 文件上传失败
**解决方案**：
- local模式：确保 `./uploads` 目录存在且有写权限
- dev模式：确认MinIO已启动且存储桶已创建

### Q6: H2数据库数据丢失
**解决方案**：
- 这是正常的，H2内存数据库在应用重启后会清空
- 如需持久化，使用MySQL（dev模式）

---

## 开发建议

### 推荐配置
- **开发调试**：使用方式一（local profile），快速启动
- **完整测试**：使用方式二（dev profile），功能完整

### 性能优化
- 使用IDEA的 `Build → Rebuild Project` 确保代码已编译
- 前端使用 `npm run dev` 支持热重载
- 后端修改代码后需要重启应用

### 调试技巧
1. **后端日志**：查看IDEA控制台输出
2. **前端日志**：查看浏览器开发者工具（F12）
3. **API测试**：使用Swagger UI或Postman

---

## 下一步

启动成功后，你可以：
1. 上传PDF文件进行测试
2. 查看PDF分析功能
3. 使用AI问答功能
4. 开始开发新功能

如有问题，请查看：
- `docs/IDEA_SETUP.md` - IDEA详细配置
- `docs/API.md` - API接口文档
- `docs/ARCHITECTURE.md` - 架构说明

