# 快速开始指南

## 最简单的方式：IDEA中直接启动（无需Docker）

### 前置要求
- ✅ Java 8+
- ✅ Maven 3.8+
- ✅ IDEA（或其他Java IDE）
- ✅ OpenAI API Key（用于AI功能）

### 步骤

#### 1. 导入项目到IDEA
- File → Open → 选择 `LitMind` 目录

#### 2. 等待Maven依赖下载
- IDEA会自动下载依赖，或手动执行：`mvn clean install`

#### 3. 配置运行参数
- 找到 `LitMindApplication.java`
- 右键 → `Run 'LitMindApplication'` → `Edit Configurations...`
- 在 `Active profiles` 填入：`local`
- 或者在 `Environment variables` 添加：`SPRING_PROFILES_ACTIVE=local`

#### 4. 配置AI API Key
编辑 `backend/src/main/resources/application-local.yml`：

```yaml
ai:
  api-key: sk-your-openai-api-key-here
```

#### 5. 启动后端
- 点击IDEA的运行按钮
- 看到 `Started LitMindApplication` 表示启动成功
- 访问：http://localhost:8080/api

#### 6. 启动前端
```bash
cd frontend
npm install
npm run dev
```

#### 7. 访问应用
- 前端：http://localhost:3030
- 登录：`admin` / `admin123`

---

## 使用Docker的完整方式

### 前置要求
- ✅ Docker Desktop
- ✅ 其他同上

### 步骤

#### 1. 启动基础设施
```bash
docker-compose up -d
```

#### 2. 配置MinIO
- 访问 http://localhost:9001
- 登录：`minioadmin` / `minioadmin123`
- 创建存储桶：`litmind-files`

#### 3. 配置AI API Key
编辑 `backend/src/main/resources/application-dev.yml`

#### 4. 在IDEA中启动
- 使用默认配置（`dev` profile）
- 或设置 `Active profiles: dev`

#### 5. 启动前端（同上）

---

## 常见问题

### Q: 启动报错 "Connection refused"
**A**: 检查是否使用了 `local` profile，如果使用 `dev` profile需要先启动Docker服务。

### Q: H2数据库数据丢失
**A**: 这是正常的，H2内存数据库在应用重启后会清空。如需持久化，使用MySQL。

### Q: AI功能不工作
**A**: 检查API Key是否正确配置，以及网络连接是否正常。

### Q: 文件上传失败
**A**: 如果使用 `local` profile，确保 `./uploads` 目录存在且有写权限。

