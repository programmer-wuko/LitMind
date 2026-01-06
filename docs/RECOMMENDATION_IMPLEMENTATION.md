# 推荐功能实现说明

## 功能概述

已实现基于用户行为和文件内容相似度的智能推荐系统，能够：
- 分析用户已查看/上传的PDF文件主题
- 基于主题相似度推荐系统内其他文件
- 考虑部门限制（仅推荐同部门文件）
- 自动记录用户行为并生成推荐

## 实现原理

### 1. 用户行为记录

系统在以下场景自动记录用户行为：
- **PDF查看**：用户查看PDF分析结果时记录 `VIEW` 行为
- **PDF分析**：用户触发PDF分析时记录 `ANALYZE` 行为
- **文件上传**：用户上传文件时记录 `UPLOAD` 行为

### 2. 推荐算法

#### 算法流程

1. **提取用户兴趣主题**
   - 从用户已查看的PDF文件中提取PDF分析结果
   - 合并研究背景、核心内容、实验结果等信息
   - 提取关键词（去除停用词）

2. **查找候选文件**
   - 获取同部门的公共PDF文件
   - 排除用户已查看的文件
   - 排除用户自己上传的文件

3. **计算相似度分数**
   - 对每个候选文件，提取其PDF分析结果
   - 使用Jaccard相似度算法计算与用户兴趣主题的相似度
   - 相似度 = 共同关键词数 / 总关键词数

4. **生成推荐**
   - 按相似度分数排序
   - 取Top 10作为推荐结果
   - 保存到数据库

#### 新用户处理

如果用户没有查看历史：
- 推荐同部门的热门文件（基于所有用户的查看次数）
- 按查看次数排序，取Top 10

### 3. 自动推荐生成

推荐在以下时机自动生成：
- **PDF分析完成后**：延迟2秒后自动生成推荐（异步）
- **手动触发**：调用 `/recommendations/generate` API

## 代码结构

### 核心类

1. **RecommendationService**
   - `calculateRecommendations()` - 核心推荐算法
   - `extractTopicsFromFiles()` - 提取文件主题
   - `calculateSimilarityScore()` - 计算相似度
   - `recommendPopularFiles()` - 推荐热门文件

2. **PdfController**
   - 在PDF查看和分析时记录用户行为
   - 分析完成后自动触发推荐生成

3. **FileService**
   - 在文件上传时记录用户行为

## 使用方法

### 1. 查看推荐

前端调用：
```typescript
GET /api/recommendations
```

返回当前用户的所有推荐记录，按推荐分数降序排列。

### 2. 手动生成推荐

前端调用：
```typescript
POST /api/recommendations/generate
```

手动触发推荐生成（会删除旧推荐并生成新推荐）。

### 3. 用户反馈

前端调用：
```typescript
PUT /api/recommendations/{id}/feedback
Body: { "feedback": "LIKE" | "DISLIKE" | "NOT_INTERESTED" }
```

记录用户对推荐的反馈（可用于优化推荐算法）。

## 推荐结果说明

推荐记录包含以下信息：
- `recommendedFileId` - 推荐的文件ID（系统内文件）
- `paperTitle` - 文件名称
- `paperSource` - "系统内文件"
- `paperUrl` - `/pdf/{fileId}` - 前端PDF查看链接
- `recommendationReason` - 推荐理由
- `recommendationScore` - 推荐分数（0-1之间，越高越相关）

## 配置说明

在 `application-local.yml` 中：
```yaml
recommendation:
  enabled: false  # 当前为false，但代码已实现，可以设置为true启用
  batch-size: 10
  cache-ttl: 3600
```

**注意**：即使 `enabled: false`，推荐功能代码仍然可以正常工作，只是不会自动触发。

## 性能优化

1. **Redis缓存**：推荐结果缓存1小时（如果Redis可用）
2. **异步生成**：推荐生成在后台线程执行，不阻塞主流程
3. **批量处理**：一次性处理所有推荐，减少数据库操作

## 限制说明

1. **仅推荐PDF文件**：当前只推荐PDF类型的文件
2. **仅同部门可见**：只推荐同部门的公共文件
3. **需要PDF分析**：文件必须有PDF分析结果才能参与推荐
4. **关键词提取简化**：当前使用简单的关键词提取，可以优化为使用AI提取更准确的主题

## 未来优化方向

1. **集成外部API**：集成arXiv、Semantic Scholar等API推荐外部论文
2. **AI主题提取**：使用AI服务提取更准确的主题关键词
3. **协同过滤**：基于相似用户的兴趣推荐
4. **深度学习**：使用深度学习模型计算相似度
5. **实时推荐**：文件上传后立即生成推荐

## 测试建议

1. **上传多个PDF文件**：确保文件有PDF分析结果
2. **查看PDF文件**：触发用户行为记录
3. **生成推荐**：调用推荐生成API
4. **查看推荐结果**：验证推荐是否合理

