# AI配置问题排查指南

## 问题：已配置AI API Key，但仍显示默认模拟结果

### 可能原因1：环境变量未正确加载

**检查步骤**：

1. **确认环境变量配置位置**
   - 必须在IDEA的**运行配置（Run Configuration）**中设置
   - 不是在系统环境变量中设置

2. **检查运行配置**
   - 打开IDEA
   - 点击右上角运行配置下拉菜单
   - 选择 `Edit Configurations...`
   - 找到您的Spring Boot运行配置
   - 检查 `Environment variables` 中是否有：
     ```
     AI_API_KEY=sk-your-actual-key
     AI_BASE_URL=https://api.deepseek.com/v1  # 如果使用DeepSeek
     AI_MODEL=deepseek-chat  # 如果使用DeepSeek
     ```

3. **重启服务**
   - 完全停止后端服务
   - 重新启动
   - **重要**：修改环境变量后必须重启服务才能生效

### 可能原因2：已有分析结果被缓存

如果之前已经分析过PDF，系统会直接返回已保存的分析结果（包括模拟结果）。

**解决方法**：

1. **删除旧的分析记录**
   - 访问H2控制台：http://localhost:8080/h2-console
   - 连接信息：
     - JDBC URL: `jdbc:h2:mem:litmind`
     - 用户名: `sa`
     - 密码: （空）
   - 执行SQL：
     ```sql
     DELETE FROM pdf_analysis WHERE file_id = YOUR_FILE_ID;
     ```
   - 或者删除所有分析记录：
     ```sql
     DELETE FROM pdf_analysis;
     ```

2. **或者重新上传文件**
   - 删除旧文件
   - 重新上传PDF文件
   - 系统会重新分析

### 可能原因3：API Key值不正确

**检查步骤**：

1. **查看后端日志**
   - 启动后端服务
   - 查看控制台日志
   - 如果看到：`AI API Key未配置，返回模拟分析结果`
   - 说明API Key未正确加载

2. **验证API Key格式**
   - DeepSeek: 应该以 `sk-` 开头
   - OpenAI: 应该以 `sk-` 开头
   - Ollama: 可以是任意值（如 `ollama`）

3. **测试API Key是否有效**
   - 使用curl测试：
     ```bash
     curl https://api.deepseek.com/v1/chat/completions \
       -H "Content-Type: application/json" \
       -H "Authorization: Bearer YOUR_API_KEY" \
       -d '{
         "model": "deepseek-chat",
         "messages": [{"role": "user", "content": "你好"}]
       }'
     ```

### 可能原因4：配置文件中的默认值

**检查配置文件** `application-local.yml`：

```yaml
ai:
  api-key: ${AI_API_KEY:your-api-key-here}  # 如果环境变量未设置，会使用默认值
```

如果环境变量 `AI_API_KEY` 未设置，会使用默认值 `your-api-key-here`，这会被代码识别为无效。

**解决方法**：
- 确保在IDEA运行配置中设置了 `AI_API_KEY` 环境变量
- 或者直接修改配置文件（不推荐，会暴露密钥）：
  ```yaml
  ai:
    api-key: sk-your-actual-api-key  # 直接写在这里
  ```

### 可能原因5：使用了错误的base-url或model

如果使用DeepSeek或其他服务，需要同时配置：

```yaml
ai:
  provider: openai
  api-key: ${AI_API_KEY}
  base-url: ${AI_BASE_URL:https://api.deepseek.com/v1}  # DeepSeek的地址
  model: ${AI_MODEL:deepseek-chat}  # DeepSeek的模型名
```

**检查**：
- 确认 `base-url` 是否正确
- 确认 `model` 名称是否正确

---

## 快速排查步骤

### 步骤1：检查日志

启动后端服务，查看启动日志，应该能看到：
```
ai.provider=openai
ai.api-key=sk-xxxxx  # 应该显示您的实际API Key（部分隐藏）
ai.base-url=https://api.deepseek.com/v1
ai.model=deepseek-chat
```

如果看到 `api-key=your-api-key-here`，说明环境变量未加载。

### 步骤2：添加调试日志

在 `AiService.java` 的构造函数或初始化方法中添加：

```java
@PostConstruct
public void init() {
    log.info("AI服务配置 - provider: {}, apiKey: {}, baseUrl: {}, model: {}", 
        provider, 
        apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : apiKey,
        baseUrl, 
        model);
}
```

### 步骤3：强制重新分析

1. 删除旧的分析记录（见上方）
2. 重新上传PDF文件
3. 或调用API重新分析：
   ```bash
   curl -X POST http://localhost:8080/api/pdf/YOUR_FILE_ID/analyze \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

---

## 完整配置示例

### DeepSeek API配置

**IDEA运行配置 - Environment variables**：
```
AI_API_KEY=sk-your-deepseek-api-key
AI_BASE_URL=https://api.deepseek.com/v1
AI_MODEL=deepseek-chat
```

**application-local.yml**：
```yaml
ai:
  provider: openai
  api-key: ${AI_API_KEY:your-api-key-here}
  base-url: ${AI_BASE_URL:https://api.deepseek.com/v1}
  model: ${AI_MODEL:deepseek-chat}
```

### Ollama本地配置

**application-local.yml**：
```yaml
ai:
  provider: openai
  api-key: ollama
  base-url: http://localhost:11434/v1
  model: qwen2.5:7b
```

---

## 验证配置是否生效

### 方法1：查看日志

启动服务后，触发一次PDF分析，查看日志：
- ✅ 如果看到 `调用LLM成功` 或 `OpenAI API调用成功`，说明配置正确
- ❌ 如果看到 `AI API Key未配置，返回模拟分析结果`，说明配置未生效

### 方法2：检查分析结果

如果分析结果中包含以下文字，说明使用的是模拟结果：
- "这是模拟的研究背景内容"
- "请配置AI_API_KEY环境变量以获取真实的AI分析"

真实的AI分析结果应该：
- 内容与PDF实际内容相关
- 不包含"模拟"、"配置"等提示文字
- 分析更详细、更专业

---

## 常见错误信息

### 错误1：`AI API Key未配置，返回模拟分析结果`
**原因**：环境变量未设置或未正确加载
**解决**：在IDEA运行配置中设置环境变量，并重启服务

### 错误2：`OpenAI API调用失败: HTTP 401`
**原因**：API Key无效或已过期
**解决**：检查API Key是否正确，是否有足够额度

### 错误3：`OpenAI API调用失败: HTTP 404`
**原因**：base-url或model配置错误
**解决**：检查base-url和model名称是否正确

### 错误4：`Connection refused` 或 `Timeout`
**原因**：网络连接问题或服务不可用
**解决**：检查网络连接，如果使用本地Ollama，确认服务已启动

