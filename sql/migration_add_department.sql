-- 部门功能迁移脚本
-- 执行此脚本前，请确保已执行 migration_add_is_public.sql

-- 1. 创建部门表
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL COMMENT '部门名称',
    description VARCHAR(500) COMMENT '部门描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 2. 创建默认部门（用于迁移现有数据）
INSERT INTO departments (name, description) 
VALUES ('默认部门', '系统默认部门，用于迁移现有数据')
ON DUPLICATE KEY UPDATE name=name;

-- 3. 为用户表添加 department_id 字段
ALTER TABLE users
ADD COLUMN department_id BIGINT COMMENT '所属部门ID',
ADD FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
ADD INDEX idx_department_id (department_id);

-- 4. 将现有用户关联到默认部门
UPDATE users 
SET department_id = (SELECT id FROM departments WHERE name = '默认部门' LIMIT 1)
WHERE department_id IS NULL;

-- 5. 为文件表添加 department_id 字段
ALTER TABLE files
ADD COLUMN department_id BIGINT COMMENT '所属部门ID',
ADD FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
ADD INDEX idx_department_id (department_id);

-- 6. 将现有文件关联到创建者的部门
UPDATE files f
INNER JOIN users u ON f.user_id = u.id
SET f.department_id = u.department_id
WHERE f.department_id IS NULL;

-- 7. 为文件夹表添加 department_id 字段
ALTER TABLE folders
ADD COLUMN department_id BIGINT COMMENT '所属部门ID',
ADD FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
ADD INDEX idx_department_id (department_id);

-- 8. 将现有文件夹关联到创建者的部门
UPDATE folders f
INNER JOIN users u ON f.user_id = u.id
SET f.department_id = u.department_id
WHERE f.department_id IS NULL;

