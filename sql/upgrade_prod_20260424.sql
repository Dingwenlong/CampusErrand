-- Production upgrade migration for CampusErrand.
-- Safe to run on an existing database; no data is truncated.

CREATE TABLE IF NOT EXISTS tb_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(64) NOT NULL COMMENT '管理员用户名',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    role VARCHAR(32) DEFAULT 'admin' COMMENT '角色',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    force_password_change TINYINT DEFAULT 0 COMMENT '是否强制修改密码',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员账号表';

SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_user' AND COLUMN_NAME = 'verify_status'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_user ADD COLUMN verify_status TINYINT DEFAULT 0 COMMENT ''实名认证状态：0-未提交 1-待审核 2-已通过 3-已驳回'' AFTER id_card',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_user' AND COLUMN_NAME = 'verify_reject_reason'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_user ADD COLUMN verify_reject_reason VARCHAR(255) DEFAULT NULL COMMENT ''实名驳回原因'' AFTER verify_status',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_user' AND COLUMN_NAME = 'id_card_front_image'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_user ADD COLUMN id_card_front_image VARCHAR(255) DEFAULT NULL COMMENT ''身份证正面照片'' AFTER verify_reject_reason',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_user' AND COLUMN_NAME = 'id_card_back_image'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_user ADD COLUMN id_card_back_image VARCHAR(255) DEFAULT NULL COMMENT ''身份证反面照片'' AFTER id_card_front_image',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_user' AND COLUMN_NAME = 'verify_submit_time'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_user ADD COLUMN verify_submit_time DATETIME DEFAULT NULL COMMENT ''实名提交时间'' AFTER id_card_back_image',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_user' AND COLUMN_NAME = 'verify_time'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_user ADD COLUMN verify_time DATETIME DEFAULT NULL COMMENT ''实名审核时间'' AFTER verify_submit_time',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_user' AND COLUMN_NAME = 'verify_admin_id'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_user ADD COLUMN verify_admin_id BIGINT DEFAULT NULL COMMENT ''实名审核管理员ID'' AFTER verify_time',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE tb_user SET verify_status = 2 WHERE is_verified = 1 AND (verify_status IS NULL OR verify_status = 0);

SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_user_wallet' AND COLUMN_NAME = 'version'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE tb_user_wallet ADD COLUMN version INT DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER pay_password',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
