# 故障排查指南

## 登录问题排查

### 问题：登录显示"用户名或密码错误"

#### 检查步骤

**1. 确认后端已启动**
- 检查IDEA控制台是否有 `Started LitMindApplication` 日志
- 访问 http://localhost:8080/api/swagger-ui.html 确认后端运行

**2. 检查DataInitializer是否执行**
在IDEA控制台查找以下日志：
```
创建默认管理员账户...
默认管理员账户创建成功: admin / admin123
```
或
```
默认管理员账户已存在
```

**3. 检查数据库中的用户**
如果使用 `local` profile（H2数据库）：
- 访问 http://localhost:8080/api/h2-console
- JDBC URL: `jdbc:h2:mem:litmind`
- 用户名: `sa`
- 密码: （留空）
- 执行: `SELECT * FROM users;`
- 应该能看到 `admin` 用户

**4. 验证密码哈希**
如果用户存在但登录失败，可能是密码哈希问题：
- H2数据库使用 `create-drop` 模式，每次重启会清空数据
- DataInitializer会在每次启动时重新创建用户
- 确保后端已重启，让DataInitializer执行

#### 解决方案

**方案1：重启后端服务**
1. 在IDEA中停止后端服务
2. 重新启动 `LitMindApplication`
3. 查看日志确认DataInitializer执行
4. 再次尝试登录

**方案2：手动创建用户（如果DataInitializer未执行）**
访问H2控制台，执行：
```sql
INSERT INTO users (username, password, email, nickname) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJ5C', 'admin@litmind.com', '管理员');
```

**方案3：检查后端日志**
查看IDEA控制台的错误日志，确认：
- 是否有数据库连接错误
- DataInitializer是否执行
- 登录时的具体错误信息

---

## 常见问题

### Q1: H2数据库数据丢失
**原因**: `application-local.yml` 中 `ddl-auto: create-drop`，每次重启会清空数据
**解决**: 这是正常的，DataInitializer会在启动时自动创建用户

### Q2: DataInitializer未执行
**检查**:
1. 确认使用了 `local` profile
2. 检查是否有编译错误
3. 查看启动日志

### Q3: 密码验证失败
**可能原因**:
1. 密码哈希值不匹配
2. BCrypt编码器配置问题
3. 数据库中的密码字段为空或格式错误

**解决**: 重启后端，让DataInitializer重新创建用户

---

## 调试技巧

### 1. 启用详细日志
在 `application-local.yml` 中添加：
```yaml
logging:
  level:
    com.litmind: DEBUG
    org.springframework.security: DEBUG
```

### 2. 检查用户数据
使用H2控制台查询：
```sql
SELECT id, username, password, email FROM users;
```

### 3. 测试密码编码
可以在后端代码中临时添加测试：
```java
String encoded = passwordEncoder.encode("admin123");
log.info("编码后的密码: {}", encoded);
```

---

## 快速修复

如果以上方法都不行，可以：

1. **删除H2数据库并重启**（如果使用local profile）
   - 重启后端即可（H2是内存数据库）

2. **使用MySQL数据库**（如果使用dev profile）
   - 确保Docker服务已启动
   - 检查MySQL连接是否正常
   - 执行 `sql/init.sql` 初始化数据

3. **临时禁用认证**（仅用于测试）
   - 修改 `SecurityConfig.java`，临时允许所有请求
   - 不推荐用于生产环境

