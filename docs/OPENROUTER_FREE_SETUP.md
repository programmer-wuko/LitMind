# OpenRouter免费AI服务配置指南

## 什么是OpenRouter？

OpenRouter是一个AI模型聚合平台，提供400+模型的统一API接口，包括多个**完全免费**的模型。

## 免费模型推荐

### DeepSeek R1 0528 (免费版)

- **完全免费**：$0/输入tokens，$0/输出tokens
- **模型信息**：DeepSeek R1，性能对标OpenAI o1
- **上下文长度**：163,840 tokens
- **完全开源**
- **API地址**：https://openrouter.ai/api/v1

## 配置步骤

### 步骤1：获取OpenRouter API Key

1. 访问：https://openrouter.ai/
2. 注册/登录账户（可以使用GitHub、Google等快速登录）
3. 进入API Keys页面：https://openrouter.ai/keys
4. 创建新的API Key
5. 复制API Key

### 步骤2：配置应用

#### 方法1：环境变量（推荐）

在IDEA运行配置中添加环境变量：
```
AI_API_KEY=sk-or-v1-your-openrouter-api-key
AI_BASE_URL=https://openrouter.ai/api/v1
AI_MODEL=deepseek/deepseek-r1-0528:free
```

#### 方法2：修改配置文件

编辑 `backend/src/main/resources/application-local.yml`：

```yaml
ai:
  provider: openai
  api-key: ${AI_API_KEY:sk-or-v1-your-openrouter-api-key}
  base-url: ${AI_BASE_URL:https://openrouter.ai/api/v1}
  model: ${AI_MODEL:deepseek/deepseek-r1-0528:free}
  max-tokens: 4000
  temperature: 0.7
```

### 步骤3：重启后端服务

重启后即可使用免费的AI服务！

## 其他免费模型选项

OpenRouter还提供其他免费模型，可以在配置中切换：

| 模型 | 说明 | 模型ID |
|------|------|--------|
| DeepSeek R1 0528 | 性能对标o1，完全免费 | `deepseek/deepseek-r1-0528:free` |
| Meta Llama 3.1 8B | Meta开源模型 | `meta-llama/llama-3.1-8b-instruct:free` |
| Google Gemma 2B | Google开源模型 | `google/gemma-2b-it:free` |

## 验证配置

启动后端服务后，查看日志应该看到：
```
AI服务初始化 - provider: openai, apiKey: sk-or-v1-..., baseUrl: https://openrouter.ai/api/v1, model: deepseek/deepseek-r1-0528:free
✅ AI API Key已配置，将使用真实的AI服务
```

## 优势

✅ **完全免费**：无需充值，无使用限制  
✅ **OpenAI兼容**：无需修改代码  
✅ **多模型选择**：可以切换不同的免费模型  
✅ **稳定可靠**：OpenRouter提供高可用性服务  

## 注意事项

1. **API Key格式**：OpenRouter的API Key以 `sk-or-v1-` 开头
2. **模型名称**：必须包含完整的模型路径，如 `deepseek/deepseek-r1-0528:free`
3. **请求头**：OpenRouter可能需要额外的请求头（代码已自动处理）

## 与DeepSeek直接API对比

| 特性 | DeepSeek直接API | OpenRouter |
|------|----------------|------------|
| **免费额度** | 2000万tokens（可能用完） | 完全免费，无限制 |
| **需要充值** | 额度用完后需要 | 不需要 |
| **模型选择** | 仅DeepSeek模型 | 400+模型可选 |
| **配置复杂度** | 简单 | 简单（相同） |

## 推荐

**强烈推荐使用OpenRouter**，因为：
- ✅ 完全免费，无使用限制
- ✅ 无需担心余额问题
- ✅ 可以随时切换不同的免费模型
- ✅ 配置简单，与DeepSeek API完全相同

