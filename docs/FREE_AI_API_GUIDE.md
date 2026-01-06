# 免费AI API配置指南

本文档介绍如何使用免费的AI API来配置 `AI_API_KEY`，包括多种免费选项和详细配置步骤。

## 免费AI API选项

### 方案1：使用兼容OpenAI API的免费服务（推荐）

这些服务兼容OpenAI API格式，可以直接使用，无需修改代码。

#### 1.1 OpenRouter（最推荐⭐⭐⭐⭐⭐）

**特点**：
- 完全兼容OpenAI API格式
- **完全免费，无使用限制**
- 提供400+模型选择，包括多个免费模型
- 稳定可靠，高可用性

**推荐模型**：
- `deepseek/deepseek-r1-0528:free` - DeepSeek R1免费版，性能对标OpenAI o1
- `meta-llama/llama-3.1-8b-instruct:free` - Meta Llama 3.1免费版

**配置步骤**：

1. **注册OpenRouter账号**
   - 访问：https://openrouter.ai/
   - 使用GitHub/Google快速登录

2. **获取API Key**
   - 进入：https://openrouter.ai/keys
   - 创建新的API Key（格式：`sk-or-v1-xxxxx`）

3. **配置环境变量**
   ```bash
   AI_API_KEY=sk-or-v1-your-openrouter-api-key
   AI_BASE_URL=https://openrouter.ai/api/v1
   AI_MODEL=deepseek/deepseek-r1-0528:free
   ```

4. **修改配置文件** `application-local.yml`：
   ```yaml
   ai:
     provider: openai
     api-key: ${AI_API_KEY:your-api-key-here}
     base-url: ${AI_BASE_URL:https://openrouter.ai/api/v1}
     model: ${AI_MODEL:deepseek/deepseek-r1-0528:free}
   ```

**详细文档**：参见 [OpenRouter免费配置指南](./OPENROUTER_FREE_SETUP.md)

#### 1.2 DeepSeek API（推荐⭐⭐⭐⭐）

**特点**：
- 完全兼容OpenAI API格式
- 提供免费额度：注册即送2000万tokens（活动可能有时效性）
- 性能优秀，响应速度快
- 支持中文

**注意**：
- 免费额度用完后需要充值
- 如果遇到"余额不足"错误，请检查账户余额
- 建议查看DeepSeek官方最新政策

**配置步骤**：

1. **注册账号**
   - 访问：https://platform.deepseek.com/
   - 注册并登录

2. **获取API Key**
   - 进入控制台：https://platform.deepseek.com/api_keys
   - 创建新的API Key

3. **配置环境变量**
   ```bash
   AI_API_KEY=sk-your-deepseek-api-key
   AI_BASE_URL=https://api.deepseek.com/v1
   AI_MODEL=deepseek-chat
   ```

4. **修改配置文件** `application-local.yml`：
   ```yaml
   ai:
     provider: openai
     api-key: ${AI_API_KEY:your-api-key-here}
     base-url: ${AI_BASE_URL:https://api.deepseek.com/v1}
     model: ${AI_MODEL:deepseek-chat}
   ```

#### 1.2 通义千问（阿里云）

**特点**：
- 阿里云提供，稳定可靠
- 免费额度：每月50万tokens（Qwen-7B模型）
- 兼容OpenAI API格式

**配置步骤**：

1. **注册阿里云账号**
   - 访问：https://www.aliyun.com/
   - 注册并实名认证

2. **开通PAI-EAS服务**
   - 访问：https://pai.console.aliyun.com/
   - 开通PAI-EAS服务

3. **创建API密钥**
   - 在控制台创建API密钥

4. **配置**：
   ```yaml
   ai:
     provider: openai
     api-key: ${AI_API_KEY:your-aliyun-api-key}
     base-url: ${AI_BASE_URL:https://dashscope.aliyuncs.com/compatible-mode/v1}
     model: qwen-turbo  # 或 qwen-plus
   ```

#### 1.3 百度文心一言

**特点**：
- 百度提供，中文支持好
- 有免费试用额度

**配置**：
```yaml
ai:
  provider: openai
  api-key: ${AI_API_KEY:your-baidu-api-key}
  base-url: ${AI_BASE_URL:https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat}
  model: ernie-bot-turbo
```

**注意**：百度API格式可能不完全兼容，需要修改代码。

---

### 方案2：本地部署开源LLM（完全免费，无限制）

如果您的电脑有足够的GPU（或使用CPU），可以本地部署开源LLM。

#### 2.1 使用Ollama（最简单⭐⭐⭐⭐⭐）

**特点**：
- 一键安装，开箱即用
- 支持Windows/Mac/Linux
- 完全免费，无API调用限制
- 支持多种开源模型（Llama、Mistral、Qwen等）

**安装步骤**：

1. **下载安装Ollama**
   - Windows: https://ollama.com/download
   - 或使用命令行：
     ```bash
     # Windows (PowerShell)
     winget install Ollama.Ollama
     ```

2. **下载模型**
   ```bash
   # 下载中文友好的模型（推荐）
   ollama pull qwen2.5:7b
   
   # 或下载其他模型
   ollama pull llama3.2
   ollama pull mistral
   ```

3. **启动Ollama服务**
   - Ollama会自动启动，默认端口：`http://localhost:11434`

4. **配置应用**
   ```yaml
   ai:
     provider: openai
     api-key: ollama  # 可以是任意值，Ollama不需要真正的key
     base-url: http://localhost:11434/v1
     model: qwen2.5:7b  # 或您下载的其他模型
   ```

5. **测试**
   ```bash
   # 测试Ollama是否运行
   curl http://localhost:11434/api/generate -d '{
     "model": "qwen2.5:7b",
     "prompt": "你好"
   }'
   ```

**优点**：
- ✅ 完全免费，无限制
- ✅ 数据隐私，所有处理在本地
- ✅ 无需网络连接（下载模型后）
- ✅ 响应速度快（本地处理）

**缺点**：
- ❌ 需要足够的硬件资源（建议8GB+ RAM，有GPU更好）
- ❌ 首次下载模型需要时间（几GB到几十GB）

#### 2.2 使用LM Studio（Windows/Mac图形界面）

**特点**：
- 图形界面，易于使用
- 支持多种模型
- 提供OpenAI兼容的API

**安装步骤**：

1. **下载LM Studio**
   - 访问：https://lmstudio.ai/
   - 下载并安装

2. **下载模型**
   - 在LM Studio中搜索并下载模型（如：Qwen、Llama等）

3. **启动本地服务器**
   - 在LM Studio中点击"Start Server"
   - 默认端口：`http://localhost:1234/v1`

4. **配置应用**
   ```yaml
   ai:
     provider: openai
     api-key: lm-studio  # 可以是任意值
     base-url: http://localhost:1234/v1
     model: qwen-7b  # 根据您下载的模型名称
   ```

---

### 方案3：其他免费API服务

#### 3.1 Hugging Face Inference API

**特点**：
- 提供免费推理API
- 支持多种开源模型

**配置**：
```yaml
ai:
  provider: openai
  api-key: ${AI_API_KEY:your-huggingface-token}
  base-url: https://api-inference.huggingface.co/models/meta-llama/Llama-2-7b-chat-hf
  model: meta-llama/Llama-2-7b-chat-hf
```

**注意**：Hugging Face API格式不完全兼容，可能需要修改代码。

#### 3.2 Groq API（快速免费）

**特点**：
- 提供免费额度
- 响应速度极快（使用GPU加速）
- 兼容OpenAI API

**配置**：
1. 注册：https://console.groq.com/
2. 获取API Key
3. 配置：
   ```yaml
   ai:
     provider: openai
     api-key: ${AI_API_KEY:your-groq-api-key}
     base-url: https://api.groq.com/openai/v1
     model: llama-3.1-8b-instant
   ```

---

## 推荐配置方案

### 方案A：快速开始（推荐新手）

**使用DeepSeek API**
- ✅ 配置简单，5分钟搞定
- ✅ 免费额度充足（200万tokens/月）
- ✅ 性能优秀
- ✅ 完全兼容，无需修改代码

### 方案B：完全免费（推荐有GPU的用户）

**使用Ollama本地部署**
- ✅ 完全免费，无限制
- ✅ 数据隐私
- ✅ 无需网络（下载模型后）

### 方案C：混合方案

**开发环境用本地Ollama，生产环境用DeepSeek**
- 本地开发：快速、免费、无限制
- 生产环境：稳定、可靠、有保障

---

## 详细配置步骤（以DeepSeek为例）

### 步骤1：获取API Key

1. 访问 https://platform.deepseek.com/
2. 注册/登录账号
3. 进入API Keys页面：https://platform.deepseek.com/api_keys
4. 点击"Create API Key"
5. 复制生成的API Key（格式：`sk-xxxxx`）

### 步骤2：在IDEA中配置环境变量

1. 打开IDEA
2. 点击右上角的运行配置下拉菜单
3. 选择 `Edit Configurations...`
4. 找到您的Spring Boot运行配置（如：`LitMindApplication`）
5. 在 `Environment variables` 中添加：
   ```
   AI_API_KEY=sk-your-deepseek-api-key-here
   AI_BASE_URL=https://api.deepseek.com/v1
   AI_MODEL=deepseek-chat
   ```
6. 点击 `OK` 保存

### 步骤3：修改配置文件（可选）

编辑 `backend/src/main/resources/application-local.yml`：

```yaml
ai:
  provider: openai
  api-key: ${AI_API_KEY:your-api-key-here}
  base-url: ${AI_BASE_URL:https://api.deepseek.com/v1}
  model: ${AI_MODEL:deepseek-chat}
  max-tokens: 4000
  temperature: 0.7
```

### 步骤4：重启后端服务

1. 停止当前运行的后端服务
2. 重新启动
3. 查看日志，确认AI服务配置成功

### 步骤5：测试

1. 上传一个PDF文件
2. 进入PDF查看页面
3. 系统会自动触发分析
4. 如果配置正确，会看到真实的AI分析结果

---

## 本地部署Ollama详细步骤

### Windows安装Ollama

1. **下载安装**
   - 访问：https://ollama.com/download/windows
   - 下载 `OllamaSetup.exe`
   - 运行安装程序

2. **验证安装**
   ```powershell
   ollama --version
   ```

3. **下载模型**
   ```powershell
   # 推荐：中文友好的Qwen模型
   ollama pull qwen2.5:7b
   
   # 或下载更小的模型（如果内存不足）
   ollama pull qwen2.5:3b
   ```

4. **启动服务**
   - Ollama安装后会自动启动
   - 默认运行在：`http://localhost:11434`

5. **测试**
   ```powershell
   ollama run qwen2.5:7b "你好，请介绍一下你自己"
   ```

6. **配置应用**
   
   编辑 `application-local.yml`：
   ```yaml
   ai:
     provider: openai
     api-key: ollama  # 可以是任意值
     base-url: http://localhost:11434/v1
     model: qwen2.5:7b
   ```

7. **验证API兼容性**
   
   创建测试文件 `test-ollama-api.ps1`：
   ```powershell
   $body = @{
       model = "qwen2.5:7b"
       messages = @(
           @{
               role = "user"
               content = "你好"
           }
       )
   } | ConvertTo-Json
   
   Invoke-RestMethod -Uri "http://localhost:11434/v1/chat/completions" `
       -Method Post `
       -ContentType "application/json" `
       -Body $body
   ```

   如果返回JSON响应，说明API兼容。

---

## 常见问题

### Q1: 如何选择最适合的方案？

**A**: 
- 如果您是新手，推荐使用 **DeepSeek API**（方案1.1）
- 如果您有GPU或足够内存，推荐使用 **Ollama本地部署**（方案2.1）
- 如果您需要完全免费且无限制，使用 **Ollama**

### Q2: Ollama需要多少内存？

**A**: 
- 7B模型：至少8GB RAM（推荐16GB）
- 3B模型：至少4GB RAM（推荐8GB）
- 有GPU会更好，可以加速推理

### Q3: 本地部署的模型响应速度如何？

**A**: 
- 有GPU：响应速度很快（1-3秒）
- 仅CPU：响应较慢（5-15秒），但可用
- 建议使用较小的模型（如3B）如果只有CPU

### Q4: 如何切换不同的AI服务？

**A**: 
只需修改 `application-local.yml` 中的配置，重启服务即可。

### Q5: 免费额度用完了怎么办？

**A**: 
- DeepSeek：每月重置，或升级付费
- Ollama：完全免费，无限制
- 其他服务：查看各自的免费额度政策

---

## 性能对比

| 方案 | 响应速度 | 成本 | 隐私性 | 稳定性 | 推荐度 |
|------|---------|------|--------|--------|--------|
| DeepSeek API | ⭐⭐⭐⭐⭐ | 免费（有限额） | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Ollama本地 | ⭐⭐⭐⭐ | 完全免费 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 通义千问 | ⭐⭐⭐⭐ | 免费（有限额） | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| Groq API | ⭐⭐⭐⭐⭐ | 免费（有限额） | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

---

## 总结

**最推荐的方案**：
1. **快速开始**：DeepSeek API（5分钟配置，立即使用）
2. **完全免费**：Ollama本地部署（一次配置，永久免费）

选择最适合您需求的方案即可！

