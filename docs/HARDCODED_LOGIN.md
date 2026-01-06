# 硬编码登录说明

## 当前实现

为了在数据库不可用时也能登录，已添加硬编码验证：

### 修改位置
**文件**: `backend/src/main/java/com/litmind/service/auth/AuthService.java`

**方法**: `login()` 方法

### 工作原理

1. **优先检查硬编码账号**：
   - 用户名: `admin`
   - 密码: `admin123`
   - 如果匹配，直接返回成功，不查询数据库

2. **如果硬编码不匹配，尝试数据库验证**：
   - 查询数据库中的用户
   - 使用Spring Security进行密码验证

### 代码位置

```java
// 临时方案：硬编码验证（不依赖数据库）
// TODO: 生产环境请移除此代码，使用数据库验证
if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
    log.info("使用硬编码验证登录成功: admin");
    // ... 直接返回token
}
```

## 使用说明

### 当前状态
- ✅ 可以使用 `admin` / `admin123` 直接登录
- ✅ 不依赖数据库
- ✅ 适用于开发测试

### 注意事项
- ⚠️ **仅用于开发环境**
- ⚠️ **生产环境必须移除硬编码验证**
- ⚠️ 硬编码账号的userId固定为 `1`

## 移除硬编码（生产环境）

当数据库配置好后，可以移除硬编码验证：

1. 打开 `backend/src/main/java/com/litmind/service/auth/AuthService.java`
2. 删除硬编码验证的if语句（第23-32行）
3. 保留数据库验证逻辑

## 测试

现在可以直接使用以下账号登录：
- **用户名**: `admin`
- **密码**: `admin123`

无需数据库支持！

