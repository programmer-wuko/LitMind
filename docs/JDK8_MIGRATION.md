# JDK 8 迁移说明

## 已完成的修改

### 1. Maven配置 (pom.xml)
- ✅ Spring Boot: `3.2.0` → `2.7.18`
- ✅ Java版本: `17` → `1.8`
- ✅ MySQL驱动: `mysql-connector-j` → `mysql-connector-java:8.0.33`
- ✅ JWT: `0.12.3` → `0.11.5` (兼容JDK 8)
- ✅ PDFBox: `3.0.1` → `2.0.29` (兼容JDK 8)
- ✅ SpringDoc: `2.3.0` → `1.7.0` (Spring Boot 2.x版本)

### 2. 包名修改
- ✅ `jakarta.*` → `javax.*` (所有实体类、DTO、Filter)
  - `jakarta.persistence.*` → `javax.persistence.*`
  - `jakarta.validation.*` → `javax.validation.*`
  - `jakarta.servlet.*` → `javax.servlet.*`

### 3. Spring Security配置
- ✅ `SecurityFilterChain` → `WebSecurityConfigurerAdapter`
- ✅ `authorizeHttpRequests()` → `authorizeRequests()`
- ✅ `requestMatchers()` → `antMatchers()`
- ✅ `EnableMethodSecurity` → `EnableGlobalMethodSecurity`
- ✅ `AuthenticationConfiguration` → `authenticationManagerBean()`

### 4. JWT服务
- ✅ `Keys.hmacShaKeyFor()` → `SecretKeySpec` (JDK 8兼容)
- ✅ `Jwts.parserBuilder()` → `Jwts.parser()` (JWT 0.11.5 API)

### 5. Java语法兼容
- ✅ `List.of()` → `Arrays.asList()` 或 `new ArrayList<>()`
- ✅ 移除了所有Java 9+特性

### 6. 文档更新
- ✅ README.md
- ✅ docs/ARCHITECTURE.md
- ✅ docs/DEPLOYMENT.md
- ✅ docs/QUICK_START.md

## 兼容性说明

### 支持的JDK版本
- ✅ **JDK 8** (最低要求)
- ✅ JDK 11 (推荐，长期支持)
- ✅ JDK 17 (如果未来升级)

### Spring Boot版本
- 当前使用: **Spring Boot 2.7.18** (最后一个支持JDK 8的版本)
- 注意: Spring Boot 3.x 要求 JDK 17+

### 依赖版本兼容性
- ✅ 所有依赖都已调整为JDK 8兼容版本
- ✅ H2数据库: 已包含，支持JDK 8
- ✅ PDFBox 2.0.29: 完全兼容JDK 8
- ✅ JWT 0.11.5: 完全兼容JDK 8

## 测试建议

1. **编译测试**
   ```bash
   cd backend
   mvn clean compile
   ```

2. **运行测试**
   ```bash
   mvn test
   ```

3. **启动应用**
   - 使用 `local` profile在IDEA中启动
   - 或使用 `dev` profile配合Docker

## 注意事项

1. **JDK版本检查**
   - 确保IDEA配置的JDK版本为1.8
   - 运行 `java -version` 确认版本

2. **Maven配置**
   - 确保Maven使用JDK 8编译
   - 检查 `pom.xml` 中的 `maven.compiler.source` 和 `maven.compiler.target`

3. **功能验证**
   - 登录功能
   - 文件上传/下载
   - PDF分析
   - AI问答

## 回退方案

如果需要回退到JDK 17，可以：
1. 恢复 `pom.xml` 中的版本配置
2. 将所有 `javax.*` 改回 `jakarta.*`
3. 恢复 Spring Security 6.x 配置
4. 升级 JWT 到 0.12.3

