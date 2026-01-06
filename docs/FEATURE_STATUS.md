# 功能状态说明

本文档说明当前系统中三个核心功能的状态和无法正常使用的原因。

## 1. 文章推荐功能

### 当前状态
**暂时无法推荐真实文章**

### 原因分析

1. **配置被禁用**
   - 在 `application-local.yml` 中，`recommendation.enabled: false`
   - 推荐功能在本地开发环境中被禁用

2. **推荐算法未完整实现**
   - `RecommendationService.calculateRecommendations()` 方法只是简化实现
   - 代码注释明确说明：
     ```java
     // 简化的推荐算法实现
     // 实际应该：
     // 1. 分析用户阅读的论文主题
     // 2. 从外部API（如arXiv、Semantic Scholar）获取相关论文
     // 3. 计算推荐分数
     // 4. 返回Top N推荐
     ```
   - 当前实现只是生成示例推荐数据（"示例推荐论文 1"、"示例推荐论文 2" 等）

3. **缺少外部数据源集成**
   - 没有集成 arXiv API
   - 没有集成 Semantic Scholar API
   - 没有集成其他学术论文数据库

4. **Redis依赖（可选）**
   - 推荐功能使用Redis缓存推荐结果以提高性能
   - 在 `local` profile中，Redis未配置，但不影响基本功能

### 解决方案

#### 方案1：启用推荐功能并实现真实推荐算法

1. **修改配置文件**
   ```yaml
   recommendation:
     enabled: true  # 启用推荐功能
   ```

2. **实现真实推荐算法**
   - 集成 arXiv API：`https://arxiv.org/help/api/user-manual`
   - 集成 Semantic Scholar API：`https://api.semanticscholar.org/`
   - 分析用户上传的PDF文件主题（使用AI提取关键词）
   - 基于主题相似度计算推荐分数

3. **示例代码结构**
   ```java
   private List<Recommendation> calculateRecommendations(Long userId, List<UserBehavior> behaviors) {
       // 1. 分析用户行为，提取感兴趣的主题
       List<String> topics = extractTopicsFromBehaviors(behaviors);
       
       // 2. 调用arXiv API获取相关论文
       List<ArxivPaper> papers = arxivApi.searchByTopics(topics);
       
       // 3. 计算推荐分数
       List<Recommendation> recommendations = papers.stream()
           .map(paper -> {
               Recommendation rec = new Recommendation();
               rec.setPaperTitle(paper.getTitle());
               rec.setPaperAuthors(paper.getAuthors());
               rec.setRecommendationScore(calculateScore(paper, topics));
               return rec;
           })
           .sorted(Comparator.comparing(Recommendation::getRecommendationScore).reversed())
           .limit(10)
           .collect(Collectors.toList());
       
       return recommendations;
   }
   ```

#### 方案2：暂时使用示例推荐（用于演示）

如果暂时不需要真实推荐，可以：
- 保持当前配置
- 前端会显示示例推荐数据
- 用于UI演示和功能测试

---

## 2. AI问答功能

### 当前状态
**需要配置AI API Key才能使用**

### 原因分析

1. **缺少API Key配置**
   - 在 `application-local.yml` 中，`ai.api-key: ${AI_API_KEY:your-api-key-here}`
   - 如果未设置 `AI_API_KEY` 环境变量，会使用默认值 `your-api-key-here`
   - `AiService.callOpenAI()` 方法会检测到无效的API Key，返回模拟结果

2. **代码逻辑**
   ```java
   if (apiKey == null || apiKey.isEmpty() || "your-api-key-here".equals(apiKey)) {
       log.warn("AI API Key未配置，返回模拟分析结果");
       return getMockAnalysisResponse();
   }
   ```

3. **模拟结果限制**
   - 模拟结果只适用于PDF分析功能
   - AI问答功能（`answerQuestion`）没有模拟实现，会直接调用API
   - 如果API Key无效，问答功能会失败

### 解决方案

#### 方法1：配置OpenAI API Key（推荐）

1. **获取OpenAI API Key**
   - 访问 https://platform.openai.com/api-keys
   - 注册账号并创建API Key

2. **在IDEA中配置环境变量**
   - 打开运行配置（Run Configuration）
   - 找到 `Environment variables`
   - 添加：
     ```
     AI_API_KEY=sk-your-actual-api-key-here
     ```

3. **或者修改配置文件**（不推荐，会暴露密钥）
   ```yaml
   ai:
     api-key: sk-your-actual-api-key-here
   ```

#### 方法2：使用其他AI服务

如果使用兼容OpenAI API的服务（如本地部署的LLM）：

```yaml
ai:
  provider: openai
  api-key: your-api-key
  base-url: http://localhost:8000/v1  # 本地LLM服务地址
  model: your-model-name
```

#### 方法3：添加模拟问答功能（用于开发测试）

如果需要在不配置API Key的情况下测试问答功能，可以修改 `AiService.answerQuestion()` 方法：

```java
public String answerQuestion(String pdfText, String question) {
    if (apiKey == null || apiKey.isEmpty() || "your-api-key-here".equals(apiKey)) {
        log.warn("AI API Key未配置，返回模拟问答结果");
        return "这是模拟的AI问答结果。请配置AI_API_KEY环境变量以获取真实的AI回答。\n\n" +
               "您的问题：" + question + "\n\n" +
               "模拟回答：根据论文内容，这是一个很好的问题。在实际使用中，AI会基于PDF内容提供详细的回答。";
    }
    String prompt = buildQAPrompt(pdfText, question);
    return callLLM(prompt);
}
```

---

## 3. PDF自动解析功能

### 当前状态
**需要配置AI API Key才能获得真实分析结果**

### 原因分析

1. **与AI问答相同的问题**
   - 需要配置 `AI_API_KEY` 环境变量
   - 如果未配置，会返回模拟分析结果

2. **模拟结果说明**
   - 代码中提供了 `getMockAnalysisResponse()` 方法
   - 返回固定的模拟JSON数据，包含：
     - 研究背景（模拟内容）
     - 核心内容（模拟内容）
     - 实验结果分析（模拟内容）
     - 其他补充（模拟内容）

3. **PDF文本提取正常**
   - PDF文本提取功能（使用Apache PDFBox）是正常的
   - 问题在于AI分析部分需要调用外部API

### 解决方案

与AI问答功能相同，需要配置 `AI_API_KEY` 环境变量。

#### 配置步骤

1. **在IDEA运行配置中添加环境变量**
   ```
   AI_API_KEY=sk-your-actual-api-key-here
   ```

2. **重启后端服务**

3. **测试**
   - 上传一个PDF文件
   - 进入PDF查看页面
   - 系统会自动触发分析
   - 如果配置正确，会看到真实的AI分析结果

#### 验证配置是否生效

查看后端日志，如果看到：
```
AI API Key未配置，返回模拟分析结果
```
说明API Key未正确配置。

如果配置正确，日志会显示：
```
调用LLM成功
```

---

## 总结

### 功能状态对比

| 功能 | 状态 | 原因 | 解决方案 |
|------|------|------|----------|
| **文章推荐** | ❌ 无法推荐真实文章 | 1. 配置被禁用<br>2. 推荐算法未实现<br>3. 缺少外部API集成 | 1. 启用配置<br>2. 实现真实推荐算法<br>3. 集成arXiv/Semantic Scholar API |
| **AI问答** | ⚠️ 需要配置API Key | 缺少AI_API_KEY环境变量 | 配置OpenAI API Key |
| **PDF自动解析** | ⚠️ 需要配置API Key | 缺少AI_API_KEY环境变量 | 配置OpenAI API Key |

### 快速修复建议

1. **立即可以做的**：
   - 配置 `AI_API_KEY` 环境变量，启用AI问答和PDF分析功能

2. **需要开发实现的**：
   - 实现真实的文章推荐算法
   - 集成外部学术论文数据库API

3. **可选优化**：
   - 配置Redis以提高推荐功能性能
   - 添加更多AI服务提供商支持（如Anthropic Claude）

---

## 相关文档

- [AI服务配置说明](./AI_SERVICE_SETUP.md)
- [本地启动指南](./LOCAL_START.md)
- [架构设计文档](./ARCHITECTURE.md)

