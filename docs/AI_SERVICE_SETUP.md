# AI服务配置说明

## 问题：PDF分析失败（400错误）

PDF分析功能需要配置AI服务（OpenAI或其他LLM服务）。

## 配置步骤

### 方法1：环境变量（推荐）

在IDEA的运行配置中设置环境变量：

1. 打开运行配置（Run Configuration）
2. 找到 `Environment variables` 或 `VM options`
3. 添加环境变量：
   ```
   AI_API_KEY=your-actual-api-key-here
   AI_BASE_URL=https://api.openai.com/v1
   ```

### 方法2：修改配置文件

编辑 `backend/src/main/resources/application-local.yml`：

```yaml
ai:
  provider: openai
  api-key: sk-your-actual-api-key-here  # 替换为实际的API Key
  base-url: https://api.openai.com/v1
  model: gpt-4-turbo-preview
  max-tokens: 4000
  temperature: 0.7
```

### 方法3：使用其他AI服务

如果使用其他兼容OpenAI API的服务（如本地部署的LLM），可以修改：

```yaml
ai:
  provider: openai
  api-key: your-api-key
  base-url: http://localhost:8000/v1  # 本地LLM服务地址
  model: your-model-name
```

## 验证配置

配置完成后，重启后端服务，然后：

1. 上传一个PDF文件
2. 点击文件进入PDF分析页面
3. 系统会自动触发分析

## 常见错误

### 错误1：AI API Key未配置
**错误信息**：`AI服务未配置，请设置AI_API_KEY环境变量`

**解决方法**：按照上述步骤配置API Key

### 错误2：API Key无效
**错误信息**：`OpenAI API调用失败: HTTP 401`

**解决方法**：检查API Key是否正确，是否有足够的额度

### 错误3：网络连接失败
**错误信息**：`Connection refused` 或 `Timeout`

**解决方法**：
- 检查网络连接
- 如果使用代理，配置代理设置
- 检查防火墙设置

## 临时禁用AI分析

如果暂时不需要AI分析功能，可以：

1. 修改 `PdfAnalysisService.analyzePdf()` 方法
2. 返回模拟的分析结果
3. 或者在前端禁用自动分析功能

## 注意事项

- API Key是敏感信息，不要提交到Git仓库
- 建议使用环境变量或配置文件（不提交到Git）
- 生产环境必须使用有效的API Key

