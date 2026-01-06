# DeepSeek余额不足问题解决指南

## 问题现象

PDF分析时出现错误：
```
OpenAI API调用失败: status=402, body={"error":{"message":"Insufficient Balance"...}}
```

## 问题原因

DeepSeek账户余额不足。可能的原因：
1. 免费额度（2000万tokens）已用完
2. 需要先充值激活账户
3. 免费活动已过期

## 解决步骤

### 步骤1：检查账户余额

1. 访问：https://platform.deepseek.com/
2. 登录您的账户
3. 查看账户余额/使用情况
4. 确认是否还有免费额度

### 步骤2：选择解决方案

#### 方案A：充值DeepSeek账户

如果免费额度已用完，可以：
1. 在DeepSeek平台充值
2. 充值后即可继续使用

#### 方案B：使用Ollama本地部署（推荐，完全免费）

如果不想充值，可以使用本地部署的Ollama：

**安装步骤**：

1. **下载安装Ollama**
   - Windows: https://ollama.com/download/windows
   - 下载并运行 `OllamaSetup.exe`

2. **下载模型**
   ```powershell
   # 推荐：中文友好的7B模型（需要8GB+内存）
   ollama pull qwen2.5:7b
   
   # 如果内存不足，使用3B模型（需要4GB+内存）
   ollama pull qwen2.5:3b
   ```

3. **测试Ollama**
   ```powershell
   ollama run qwen2.5:7b "你好"
   ```

4. **修改配置文件**

   编辑 `backend/src/main/resources/application-local.yml`：
   ```yaml
   ai:
     provider: openai
     api-key: ollama  # 可以是任意值
     base-url: http://localhost:11434/v1
     model: qwen2.5:7b  # 或 qwen2.5:3b
   ```

5. **重启后端服务**

   完成！现在完全免费，无限制使用！

## 两种方案对比

| 特性 | DeepSeek API | Ollama本地 |
|------|-------------|-----------|
| **成本** | 免费额度用完后需充值 | 完全免费 |
| **响应速度** | 快（云端） | 快（本地，有GPU） |
| **数据隐私** | 云端处理 | 完全本地 |
| **网络要求** | 需要 | 不需要（下载后） |
| **硬件要求** | 无 | 8GB+ RAM（7B模型） |

## 推荐

- **快速解决**：充值DeepSeek账户
- **长期使用**：配置Ollama本地部署（一次配置，永久免费）

