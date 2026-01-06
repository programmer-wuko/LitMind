# IDEA 中直接启动指南（无需Docker）

## 方式一：使用简化配置（推荐，无需安装任何服务）

### 1. 添加H2数据库依赖

已在 `pom.xml` 中添加H2依赖，无需额外操作。

### 2. 配置IDEA运行参数

1. 打开IDEA，找到 `LitMindApplication.java`
2. 右键 → `Run 'LitMindApplication'`
3. 点击运行配置旁边的下拉菜单 → `Edit Configurations...`
4. 在 `Environment variables` 或 `VM options` 中添加：
   ```
   -Dspring.profiles.active=local
   ```
   或者在 `Active profiles` 中填入：`local`

### 3. 配置AI API Key

编辑 `backend/src/main/resources/application-local.yml`，设置你的OpenAI API Key：

```yaml
ai:
  api-key: sk-your-actual-api-key-here
```

或者设置环境变量：
- Windows: `set AI_API_KEY=sk-your-key`
- Linux/Mac: `export AI_API_KEY=sk-your-key`

### 4. 启动项目

直接点击IDEA的运行按钮即可！

**特点**：
- ✅ 使用H2内存数据库（无需MySQL）
- ✅ 使用本地文件系统存储（无需MinIO）
- ✅ Redis和Kafka可选（已禁用）
- ✅ 数据在应用重启后清空（H2内存数据库）

### 5. 访问H2控制台（可选）

启动后访问：http://localhost:8080/api/h2-console
- JDBC URL: `jdbc:h2:mem:litmind`
- 用户名: `sa`
- 密码: （留空）

---

## 方式二：使用Docker（完整功能）

如果你想要完整功能（Redis缓存、Kafka异步处理、MinIO对象存储），需要：

### 1. 安装Docker Desktop

下载地址：https://www.docker.com/products/docker-desktop

### 2. 启动基础设施

```bash
docker-compose up -d
```

### 3. 配置MinIO

访问 http://localhost:9001，创建存储桶 `litmind-files`

### 4. 在IDEA中启动

使用默认配置（`application-dev.yml`）即可，无需修改运行参数。

---

## 方式三：本地安装服务（不推荐）

如果你不想用Docker，也可以本地安装：
- MySQL 8.0
- Redis 7
- Kafka 3
- MinIO

然后修改配置文件中的连接信息。

---

## 推荐方案对比

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **方式一（H2+本地文件）** | 无需安装任何服务，快速启动 | 数据不持久化，功能受限 | 开发调试、快速验证 |
| **方式二（Docker）** | 功能完整，环境一致 | 需要安装Docker | 完整开发、测试环境 |
| **方式三（本地安装）** | 性能好，可定制 | 安装配置复杂 | 生产环境 |

**建议**：开发阶段使用方式一，需要完整测试时使用方式二。

