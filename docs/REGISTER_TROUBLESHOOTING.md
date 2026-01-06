# 注册功能问题排查指南

## 404 错误排查

如果遇到 "Request failed with status code 404" 错误，请按以下步骤排查：

### 1. 检查后端服务是否启动

- 确认后端服务运行在 `http://localhost:8080`
- 访问 `http://localhost:8080/api/swagger-ui.html` 查看 API 文档
- 检查后端控制台是否有错误日志

### 2. 检查数据库迁移

**重要：** 必须执行数据库迁移脚本才能使用注册功能！

#### 如果使用 H2 数据库（local profile）：
- H2 数据库会在应用启动时自动创建表结构
- 但需要确保 `Department` 实体已正确配置
- 检查后端启动日志，确认表创建成功

#### 如果使用 MySQL 数据库（dev profile）：
执行以下 SQL 脚本：
```sql
-- 执行 sql/migration_add_department.sql
```

### 3. 检查 API 路径

前端请求路径：
- 注册接口：`POST http://localhost:8080/api/auth/register`
- 部门列表：`GET http://localhost:8080/api/departments`

后端路由配置：
- Context Path: `/api` (在 `application.yml` 中配置)
- 注册接口：`/auth/register`
- 部门接口：`/departments`

### 4. 检查浏览器控制台

打开浏览器开发者工具（F12），查看：
- Network 标签：查看具体的请求 URL 和响应状态
- Console 标签：查看 JavaScript 错误信息

### 5. 检查后端日志

查看后端控制台输出，特别关注：
- 是否有 "用户注册成功" 的日志
- 是否有异常堆栈信息
- 是否有数据库连接错误

## 常见错误及解决方案

### 错误 1: "请选择部门或创建新部门"

**原因：** 注册时既没有选择现有部门，也没有输入新部门名称

**解决：** 
- 选择 "选择现有部门" 并选择一个部门，或
- 选择 "创建新部门" 并输入部门名称

### 错误 2: "用户名已存在"

**原因：** 用户名已被注册

**解决：** 使用不同的用户名

### 错误 3: "部门不存在"

**原因：** 选择的部门 ID 在数据库中不存在

**解决：** 
- 刷新页面重新加载部门列表
- 或选择 "创建新部门"

### 错误 4: 加载部门列表失败

**原因：** 
- 后端服务未启动
- 数据库未初始化
- 网络连接问题

**解决：**
- 检查后端服务状态
- 执行数据库迁移脚本
- 检查网络连接
- 即使加载失败，也可以手动创建新部门

## 测试步骤

1. **启动后端服务**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **确认后端运行**
   - 访问 `http://localhost:8080/api/swagger-ui.html`
   - 测试 `GET /api/departments` 接口

3. **启动前端服务**
   ```bash
   cd frontend
   npm run dev
   ```

4. **访问注册页面**
   - 打开 `http://localhost:3030/register`
   - 检查部门列表是否加载成功

5. **填写注册信息**
   - 用户名：至少 3 个字符
   - 密码：至少 6 个字符
   - 选择部门或创建新部门

6. **提交注册**
   - 查看浏览器控制台的网络请求
   - 查看后端日志

## 调试技巧

### 查看详细错误信息

在浏览器控制台运行：
```javascript
// 查看 API 客户端配置
console.log('API URL:', process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api')
```

### 测试后端接口

使用 curl 或 Postman 测试：
```bash
# 测试部门列表接口
curl http://localhost:8080/api/departments

# 测试注册接口
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "newDepartmentName": "测试部门"
  }'
```

## 联系支持

如果以上步骤都无法解决问题，请提供：
1. 后端控制台的完整错误日志
2. 浏览器控制台的错误信息
3. Network 标签中的请求详情（URL、状态码、响应内容）

