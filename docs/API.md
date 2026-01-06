# API 文档

## 认证接口

### POST /api/auth/login

用户登录

**请求体**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "admin",
    "userId": 1
  }
}
```

## 文件管理接口

### GET /api/files

获取文件列表

**查询参数**:
- `folderId` (可选): 文件夹ID

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "paper.pdf",
      "fileSize": 1024000,
      "fileType": "application/pdf",
      "folderId": null,
      "createdAt": "2024-01-01T00:00:00"
    }
  ]
}
```

### POST /api/files/upload

上传文件

**请求**: multipart/form-data
- `file`: 文件
- `folderId` (可选): 文件夹ID

**响应**:
```json
{
  "code": 200,
  "message": "文件上传成功",
  "data": {
    "id": 1,
    "name": "paper.pdf",
    "fileSize": 1024000,
    "fileType": "application/pdf"
  }
}
```

### DELETE /api/files/{id}

删除文件

## 文件夹管理接口

### GET /api/folders

获取文件夹列表

**查询参数**:
- `parentId` (可选): 父文件夹ID

### POST /api/folders

创建文件夹

**请求体**:
```json
{
  "name": "新文件夹",
  "parentId": null
}
```

### DELETE /api/folders/{id}

删除文件夹

## PDF分析接口

### GET /api/pdf/{fileId}/analysis

获取PDF分析结果

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "fileId": 1,
    "researchBackground": "研究背景内容...",
    "coreContent": "核心内容...",
    "experimentResults": "实验结果...",
    "additionalInfo": "其他补充...",
    "analysisStatus": "COMPLETED"
  }
}
```

### POST /api/pdf/{fileId}/analyze

触发PDF分析

### PUT /api/pdf/{fileId}/analysis

更新PDF分析结果

**请求体**:
```json
{
  "researchBackground": "更新后的研究背景",
  "coreContent": "更新后的核心内容",
  "experimentResults": "更新后的实验结果",
  "additionalInfo": "更新后的其他补充"
}
```

## AI问答接口

### POST /api/ai/qa

AI问答

**请求体**:
```json
{
  "fileId": 1,
  "question": "本文用了什么评估指标？"
}
```

**响应**:
```json
{
  "code": 200,
  "data": "本文使用了准确率、精确率和召回率作为评估指标..."
}
```

## 推荐接口

### GET /api/recommendations

获取推荐列表

### POST /api/recommendations/generate

生成推荐

### PUT /api/recommendations/{id}/feedback

更新推荐反馈

**请求体**:
```json
{
  "feedback": "LIKE"
}
```

