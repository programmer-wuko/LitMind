# 解决MySQL连接失败问题

## 问题原因

从错误日志可以看到：
- 应用使用了 `dev` profile
- 尝试连接MySQL：`localhost:3306`
- 连接失败：`Connection refused: connect`

这是因为 `dev` profile 需要MySQL服务，但MySQL没有启动。

---

## 解决方案一：使用 local profile（推荐，无需Docker）

### 步骤1：修改IDEA运行配置

1. 在IDEA中，找到运行配置（右上角）
2. 点击运行配置下拉菜单 → `Edit Configurations...`
3. 找到 `LitMindApplication` 配置
4. 在 `Active profiles` 字段中填入：`local`
5. 点击 `OK`

### 步骤2：重新启动

点击运行按钮重新启动应用。

**特点**：
- ✅ 使用H2内存数据库（无需MySQL）
- ✅ 使用本地文件系统（无需MinIO）
- ✅ 无需启动Docker

---

## 解决方案二：启动Docker服务（如果想用dev profile）

如果你想使用完整的 `dev` profile功能，需要先启动Docker：

### 步骤1：启动Docker服务
```bash
docker-compose up -d
```

### 步骤2：等待服务启动
等待MySQL、Redis、Kafka、MinIO全部启动完成（约30秒）

### 步骤3：配置MinIO
1. 访问 http://localhost:9001
2. 登录：`minioadmin` / `minioadmin123`
3. 创建存储桶：`litmind-files`

### 步骤4：重新启动应用
在IDEA中重新运行应用即可。

---

## 快速检查

### 检查当前使用的profile
查看启动日志的第一行：
```
The following 1 profile is active: "dev"  ← 这里显示当前profile
```

### 确认local profile配置
如果使用 `local` profile，应该看到：
- H2数据库连接成功
- 没有MySQL连接错误
- 日志显示：`HHH000400: Using dialect: org.hibernate.dialect.H2Dialect`

---

## 推荐方案

**开发调试**：使用 `local` profile（方案一）
- 快速启动
- 无需Docker
- 适合本地开发

**完整测试**：使用 `dev` profile + Docker（方案二）
- 功能完整
- 需要Docker
- 适合集成测试

