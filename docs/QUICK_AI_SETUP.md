# 快速AI配置指南（5分钟搞定）

## 🚀 方案1：DeepSeek API（最简单，推荐）

### 步骤1：获取API Key（2分钟）

1. 访问：https://platform.deepseek.com/
2. 注册/登录（支持微信/邮箱注册）
3. 进入：https://platform.deepseek.com/api_keys
4. 点击"Create API Key"
5. 复制API Key（格式：`sk-xxxxx`）

### 步骤2：配置环境变量（1分钟）

在IDEA运行配置中添加：
```
AI_API_KEY=sk-your-deepseek-api-key
AI_BASE_URL=https://api.deepseek.com/v1
AI_MODEL=deepseek-chat
```

### 步骤3：重启服务（1分钟）

重启后端，完成！

**免费额度**：注册即送2000万tokens（活动可能有时效性，请查看官方最新政策）

---

## 🆓 方案2：Ollama本地部署（完全免费）

### 步骤1：安装Ollama（2分钟）

**Windows**：
1. 访问：https://ollama.com/download/windows
2. 下载并运行 `OllamaSetup.exe`
3. 安装完成

**验证安装**：
```powershell
ollama --version
```

### 步骤2：下载模型（5-10分钟，取决于网速）

```powershell
# 推荐：中文友好的7B模型（需要8GB+内存）
ollama pull qwen2.5:7b

# 如果内存不足，使用3B模型（需要4GB+内存）
ollama pull qwen2.5:3b
```

### 步骤3：测试（1分钟）

```powershell
ollama run qwen2.5:7b "你好"
```

### 步骤4：配置应用（1分钟）

编辑 `backend/src/main/resources/application-local.yml`：

```yaml
ai:
  provider: openai
  api-key: ollama  # 可以是任意值
  base-url: http://localhost:11434/v1
  model: qwen2.5:7b  # 或 qwen2.5:3b
```

### 步骤5：重启服务

完成！现在完全免费，无限制使用！

---

## 📊 两种方案对比

| 特性 | DeepSeek API | Ollama本地 |
|------|-------------|-----------|
| **配置时间** | 5分钟 | 10-15分钟 |
| **成本** | 免费（200万tokens/月） | 完全免费 |
| **响应速度** | 快（云端） | 快（本地，有GPU） |
| **数据隐私** | 云端处理 | 完全本地 |
| **网络要求** | 需要 | 不需要（下载后） |
| **硬件要求** | 无 | 8GB+ RAM（7B模型） |

---

## ✅ 推荐选择

- **新手/快速开始**：选择 **DeepSeek API**
- **需要完全免费/数据隐私**：选择 **Ollama本地**

两个方案都完全兼容，无需修改代码！

