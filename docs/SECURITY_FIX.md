# 安全漏洞修复说明

## 已修复的安全漏洞

### 1. Next.js 严重漏洞
- **版本**: `14.2.18` → `14.2.35`
- **修复**: 多个DoS、SSRF、授权绕过等严重漏洞
- **影响**: 所有Next.js功能

### 2. PDF.js 高危漏洞
- **版本**: `3.11.174` → `4.9.129`
- **修复**: 恶意PDF可执行任意JavaScript
- **影响**: PDF查看器安全

### 3. react-pdf 升级
- **版本**: `7.7.3` → `9.1.2`
- **原因**: 支持新的pdfjs-dist版本
- **注意**: API有变化，已更新代码

## 代码更新

### PdfViewer组件更新
- ✅ 更新了worker配置方式（react-pdf v9新API）
- ✅ 更新了CSS导入路径
- ✅ 更新了httpHeaders配置方式

## 安装步骤

### 1. 删除旧依赖
```powershell
Remove-Item -Recurse -Force node_modules, package-lock.json
```

### 2. 重新安装
```powershell
npm install
```

### 3. 验证
```powershell
npm run build
```

## 注意事项

### react-pdf v9 变化
1. **Worker配置**: 使用 `new URL()` 方式
2. **CSS路径**: 从 `dist/esm/Page/` 改为 `dist/Page/`
3. **httpHeaders**: 从 `options.httpHeaders` 改为直接属性

### 如果遇到问题

#### 问题1: PDF无法加载
检查worker配置是否正确，可能需要使用CDN：
```typescript
pdfjs.GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/4.9.129/pdf.worker.min.js`
```

#### 问题2: 类型错误
运行：
```powershell
npm install --save-dev @types/react-pdf
```

#### 问题3: 构建失败
清理缓存：
```powershell
Remove-Item -Recurse -Force .next
npm run build
```

## 剩余警告

以下警告是依赖包的间接依赖，不影响功能：
- `inflight`, `rimraf`, `glob` 等：这些是构建工具的依赖，在生产环境不会使用
- `eslint@8`: 虽然已废弃，但与Next.js 14兼容性最好

## 安全状态

✅ **已修复**: Next.js和PDF.js的所有已知安全漏洞
⚠️ **警告**: 一些构建工具依赖的警告（不影响生产环境）

