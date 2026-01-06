# 文件分类管理系统设计文档

## 1. 设计概述

系统支持两个文件管理模块：
- **文献管理**：所有人可见的公共文献库
- **我的文献**：个人可见的私有文献库

## 2. 数据库设计

### 2.1 字段添加

在 `files` 和 `folders` 表中添加 `is_public` 字段：

```sql
ALTER TABLE files ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE folders ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT FALSE;
```

### 2.2 字段说明

- `is_public = false`：仅"我的文献"可见（个人私有）
- `is_public = true`：在"文献管理"中可见（所有人可见）
- 如果文件同时满足 `is_public = true` 且 `userId = currentUserId`，则两个模块都会显示

## 3. 后端实现

### 3.1 Repository 层

**FileRepository**:
- `findByIsPublicTrue()` - 查询所有公共文件
- `findByIsPublicTrueAndFolderId(Long folderId)` - 查询指定文件夹的公共文件

**FolderRepository**:
- `findByIsPublicTrue()` - 查询所有公共文件夹
- `findByIsPublicTrueAndParentId(Long parentId)` - 查询指定父文件夹的公共文件夹

### 3.2 Service 层

**FileService**:
- `getPublicFiles(Long folderId)` - 获取公共文件（文献管理）
- `getMyFiles(Long userId, Long folderId)` - 获取用户的文件（我的文献，包括公共和私有）
- `uploadFile(Long userId, Long folderId, MultipartFile file, Boolean isPublic)` - 上传文件时指定可见性

**FolderService**:
- `getPublicFolders(Long parentId)` - 获取公共文件夹（文献管理）
- `getMyFolders(Long userId, Long parentId)` - 获取用户的文件夹（我的文献，包括公共和私有）
- `createFolder(Long userId, Long parentId, String name, Boolean isPublic)` - 创建文件夹时指定可见性

### 3.3 Controller 层

**FileController**:
- `GET /files?folderId=xxx&isPublic=true` - 获取公共文件
- `GET /files?folderId=xxx&isPublic=false` - 获取用户的文件
- `POST /files/upload?folderId=xxx&isPublic=true` - 上传文件并指定可见性

**FolderController**:
- `GET /folders?parentId=xxx&isPublic=true` - 获取公共文件夹
- `GET /folders?parentId=xxx&isPublic=false` - 获取用户的文件夹
- `POST /folders` - 创建文件夹，请求体中包含 `isPublic` 字段

## 4. 前端实现

### 4.1 上传位置选择

**FileUpload 组件**支持三种上传模式：
- `defaultLocation="my"` - 仅我的文献（默认）
- `defaultLocation="public"` - 仅文献管理
- `defaultLocation="both"` - 两者都上传（显示选择对话框）

**上传逻辑**：
- 选择"仅我的文献"：`isPublic = false`
- 选择"仅文献管理"：`isPublic = true`
- 选择"两者都上传"：`isPublic = true`（这样文件会在两个模块都显示）

### 4.2 页面查询逻辑

**文献管理页面** (`/dashboard`):
- 查询公共文件：`fileApi.getFiles(folderId, true)`
- 查询公共文件夹：`fileApi.getFolders(undefined, true)`
- 上传默认位置：`defaultLocation="public"`

**我的文献页面** (`/dashboard/my-documents`):
- 查询用户的文件：`fileApi.getFiles(folderId, false)`
- 查询用户的文件夹：`fileApi.getFolders(undefined, false)`
- 上传默认位置：`defaultLocation="both"`（显示选择对话框）

## 5. 使用场景

### 场景1：在文献管理上传文件
- 用户选择"仅文献管理"
- 文件 `isPublic = true`
- 文件在文献管理中显示，所有人可见
- 文件也在"我的文献"中显示（因为 `userId = currentUserId`）

### 场景2：在我的文献上传文件
- 用户选择"仅我的文献"
- 文件 `isPublic = false`
- 文件仅在"我的文献"中显示

### 场景3：同时上传到两个模块
- 用户选择"两者都上传"
- 文件 `isPublic = true`
- 文件在两个模块都显示

## 6. 文件夹管理

文件夹的可见性规则与文件相同：
- 公共文件夹：`isPublic = true`，在文献管理中可见
- 私有文件夹：`isPublic = false`，仅在我的文献中可见
- 如果文件夹 `isPublic = true` 且 `userId = currentUserId`，则两个模块都显示

## 7. 数据库迁移

执行 `sql/migration_add_is_public.sql` 脚本添加字段。

## 8. 注意事项

1. **文件同步**：如果文件 `isPublic = true` 且属于当前用户，会自动在两个模块显示，无需创建两个文件记录
2. **权限控制**：只有文件/文件夹的所有者可以修改可见性
3. **文件夹继承**：子文件夹的可见性可以独立于父文件夹设置

