-- 管理员表
CREATE TABLE IF NOT EXISTS tb_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    role VARCHAR(32) DEFAULT 'ADMIN' COMMENT '角色',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    force_password_change TINYINT DEFAULT 0 COMMENT '是否强制修改密码：0-否 1-是',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    UNIQUE KEY uk_username (username),
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 插入默认管理员账户（用户名 admin，密码 admin123）
-- 注意：实际使用时需要用 BCrypt 加密，这里先用明文演示
INSERT INTO tb_admin (username, password_hash, nickname, role, status, force_password_change) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt5CzGq', '系统管理员', 'ADMIN', 1, 0);
