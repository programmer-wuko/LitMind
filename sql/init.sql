-- LitMind 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS litmind DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE litmind;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 文件夹表
CREATE TABLE IF NOT EXISTS folders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父文件夹ID，NULL表示根目录',
    name VARCHAR(255) NOT NULL COMMENT '文件夹名称',
    path VARCHAR(1000) COMMENT '完整路径',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES folders(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_path (path(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件夹表';

-- 文件表
CREATE TABLE IF NOT EXISTS files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    folder_id BIGINT DEFAULT NULL COMMENT '所属文件夹ID',
    name VARCHAR(255) NOT NULL COMMENT '文件名',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型（如：application/pdf）',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    upload_status VARCHAR(20) DEFAULT 'UPLOADING' COMMENT '上传状态：UPLOADING, COMPLETED, FAILED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_folder_id (folder_id),
    INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- PDF分析结果表
CREATE TABLE IF NOT EXISTS pdf_analyses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL COMMENT '文件ID',
    research_background TEXT COMMENT '研究背景',
    core_content TEXT COMMENT '核心内容',
    experiment_results TEXT COMMENT '实验结果分析',
    additional_info TEXT COMMENT '其他补充',
    analysis_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '分析状态：PENDING, PROCESSING, COMPLETED, FAILED',
    analysis_model VARCHAR(50) COMMENT '使用的AI模型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    UNIQUE KEY uk_file_id (file_id),
    INDEX idx_analysis_status (analysis_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PDF分析结果表';

-- 附件表
CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL COMMENT '关联的文件ID',
    name VARCHAR(255) NOT NULL COMMENT '附件名称',
    file_path VARCHAR(500) NOT NULL COMMENT '附件存储路径',
    file_size BIGINT NOT NULL COMMENT '附件大小（字节）',
    file_type VARCHAR(50) NOT NULL COMMENT '附件类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    INDEX idx_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

-- 用户行为记录表（用于推荐算法）
CREATE TABLE IF NOT EXISTS user_behaviors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    file_id BIGINT COMMENT '文件ID',
    behavior_type VARCHAR(50) NOT NULL COMMENT '行为类型：VIEW, ANALYZE, EDIT, DELETE, SEARCH',
    behavior_data JSON COMMENT '行为数据（JSON格式）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_file_id (file_id),
    INDEX idx_behavior_type (behavior_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

-- 推荐记录表
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    recommended_file_id BIGINT COMMENT '推荐的文件ID（内部文件）',
    external_paper_id VARCHAR(100) COMMENT '外部论文ID（如arXiv ID）',
    paper_title VARCHAR(500) COMMENT '论文标题',
    paper_authors TEXT COMMENT '作者列表',
    paper_source VARCHAR(100) COMMENT '来源（如：arXiv, Semantic Scholar）',
    paper_url VARCHAR(500) COMMENT '论文URL',
    recommendation_reason TEXT COMMENT '推荐理由',
    recommendation_score DECIMAL(5,2) COMMENT '推荐分数',
    feedback VARCHAR(20) COMMENT '用户反馈：LIKE, DISLIKE, NOT_INTERESTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (recommended_file_id) REFERENCES files(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_feedback (feedback),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐记录表';

-- AI问答记录表
CREATE TABLE IF NOT EXISTS ai_qa_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL COMMENT '文件ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question TEXT NOT NULL COMMENT '用户问题',
    answer TEXT COMMENT 'AI回答',
    model_used VARCHAR(50) COMMENT '使用的AI模型',
    tokens_used INT COMMENT '消耗的Token数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_file_id (file_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI问答记录表';

-- 初始化默认管理员账户（密码：admin123）
INSERT INTO users (username, password, email, nickname) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJ5C', 'admin@litmind.com', '管理员')
ON DUPLICATE KEY UPDATE username=username;

