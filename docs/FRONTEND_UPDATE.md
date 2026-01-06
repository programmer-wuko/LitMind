# 前端依赖更新说明

## 已更新的依赖

### 安全更新
- ✅ **Next.js**: `14.0.4` → `14.2.18` (修复安全漏洞)
- ✅ **ESLint Config Next**: `14.0.4` → `14.2.18` (匹配Next.js版本)

### 功能更新
- ✅ **@tanstack/react-query**: `^5.17.0` → `^5.62.0` (最新稳定版)
- ✅ **axios**: `^1.6.2` → `^1.7.9` (安全更新)
- ✅ **zustand**: `^4.4.7` → `^4.5.5` (保持v4，避免破坏性变更)
- ✅ **react-pdf**: `^7.6.0` → `^7.7.3` (最新v7版本)
- ✅ **lucide-react**: `^0.303.0` → `^0.468.0` (图标库更新)
- ✅ **clsx**: `^2.1.0` → `^2.1.1` (小版本更新)

### 开发依赖更新
- ✅ **TypeScript**: `^5.3.3` → `^5.7.2` (最新稳定版)
- ✅ **Tailwind CSS**: `^3.4.0` → `^3.4.17` (最新v3版本)
- ✅ **PostCSS**: `^8.4.32` → `^8.4.49` (安全更新)
- ✅ **Autoprefixer**: `^10.4.16` → `^10.4.20` (小版本更新)
- ✅ **@types/node**: `^20.10.6` → `^20.17.10` (类型定义更新)
- ✅ **@types/react**: `^18.2.46` → `^18.3.18` (类型定义更新)
- ✅ **@types/react-dom**: `^18.2.18` → `^18.3.5` (类型定义更新)

### 保持不变（避免破坏性变更）
- ⚠️ **ESLint**: 保持 `^8.57.1` (ESLint 9需要额外配置)
- ⚠️ **pdfjs-dist**: 保持 `^3.11.174` (react-pdf 7.x兼容版本)
- ⚠️ **react-pdf**: 保持 v7.x (v9.x有API变更)

## 下一步操作

### 1. 删除旧的node_modules和lock文件
```bash
cd frontend
rm -rf node_modules package-lock.json
```

Windows PowerShell:
```powershell
cd frontend
Remove-Item -Recurse -Force node_modules, package-lock.json
```

### 2. 重新安装依赖
```bash
npm install
```

### 3. 验证安装
```bash
npm run build
```

## 注意事项

1. **react-pdf v7**: 如果后续需要升级到v9，需要修改PDF查看器代码
2. **ESLint 8**: 虽然已废弃，但与Next.js 14兼容性最好
3. **zustand v4**: 保持v4避免破坏性变更，v5需要代码调整

## 如果遇到问题

### 问题1: 构建失败
```bash
# 清理缓存
rm -rf .next
npm run build
```

### 问题2: 类型错误
```bash
# 重新生成类型
npm run build
```

### 问题3: PDF查看器不工作
检查 `react-pdf` 和 `pdfjs-dist` 版本兼容性，可能需要降级。

