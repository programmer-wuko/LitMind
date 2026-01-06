-- 添加 isPublic 字段到 files 和 folders 表
-- 用于区分文献管理（公共）和我的文献（私有）

USE litmind;

-- 为 files 表添加 isPublic 字段
ALTER TABLE files 
ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否公开：false=仅我的文献可见, true=文献管理可见（所有人可见）';

-- 为 folders 表添加 isPublic 字段
ALTER TABLE folders 
ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否公开：false=仅我的文献可见, true=文献管理可见（所有人可见）';

-- 创建索引以提高查询性能
CREATE INDEX idx_files_is_public ON files(is_public);
CREATE INDEX idx_folders_is_public ON folders(is_public);

