-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_errand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_errand;

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    openid VARCHAR(64) NOT NULL COMMENT '微信openid',
    unionid VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    user_type TINYINT DEFAULT 0 COMMENT '用户类型：0-普通用户 1-跑腿员 2-管理员',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    is_verified TINYINT DEFAULT 0 COMMENT '是否实名认证：0-否 1-是',
    real_name VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    id_card VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    student_id VARCHAR(32) DEFAULT NULL COMMENT '学号',
    school_name VARCHAR(128) DEFAULT NULL COMMENT '学校名称',
    credit_score INT DEFAULT 100 COMMENT '信用分',
    total_orders INT DEFAULT 0 COMMENT '总订单数',
    completed_orders INT DEFAULT 0 COMMENT '已完成订单数',
    cancelled_orders INT DEFAULT 0 COMMENT '已取消订单数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_openid (openid),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户钱包表
CREATE TABLE IF NOT EXISTS tb_user_wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
    frozen_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结金额',
    total_income DECIMAL(10,2) DEFAULT 0.00 COMMENT '总收入',
    total_expense DECIMAL(10,2) DEFAULT 0.00 COMMENT '总支出',
    pay_password VARCHAR(64) DEFAULT NULL COMMENT '支付密码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';

-- 交易流水表
CREATE TABLE IF NOT EXISTS tb_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    transaction_no VARCHAR(64) NOT NULL COMMENT '交易流水号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    transaction_type TINYINT NOT NULL COMMENT '交易类型：1-充值 2-提现 3-支付 4-收入 5-退款',
    direction TINYINT NOT NULL COMMENT '资金流向：1-收入 2-支出',
    amount DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    balance DECIMAL(10,2) NOT NULL COMMENT '交易后余额',
    related_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
    related_type VARCHAR(32) DEFAULT NULL COMMENT '关联业务类型',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功 2-处理中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_transaction_no (transaction_no),
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易流水表';

-- 跑腿任务表
CREATE TABLE IF NOT EXISTS tb_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    task_type TINYINT NOT NULL COMMENT '任务类型：1-取快递 2-代买 3-送件 4-其他',
    title VARCHAR(128) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    pickup_address VARCHAR(255) NOT NULL COMMENT '取件地址',
    pickup_contact VARCHAR(64) DEFAULT NULL COMMENT '取件联系人',
    pickup_phone VARCHAR(20) DEFAULT NULL COMMENT '取件联系电话',
    pickup_longitude DECIMAL(10,7) DEFAULT NULL COMMENT '取件经度',
    pickup_latitude DECIMAL(10,7) DEFAULT NULL COMMENT '取件纬度',
    delivery_address VARCHAR(255) NOT NULL COMMENT '送达地址',
    delivery_contact VARCHAR(64) DEFAULT NULL COMMENT '送达联系人',
    delivery_phone VARCHAR(20) DEFAULT NULL COMMENT '送达联系电话',
    delivery_longitude DECIMAL(10,7) DEFAULT NULL COMMENT '送达经度',
    delivery_latitude DECIMAL(10,7) DEFAULT NULL COMMENT '送达纬度',
    reward DECIMAL(10,2) NOT NULL COMMENT '基础赏金',
    weight_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '重量附加费',
    urgency_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '加急费',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    expect_time DATETIME DEFAULT NULL COMMENT '期望送达时间',
    deadline_time DATETIME DEFAULT NULL COMMENT '截止时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待接单 1-已接单 2-待取件 3-配送中 4-待确认 5-已完成 6-已取消',
    runner_id BIGINT DEFAULT NULL COMMENT '接单跑腿员ID',
    accept_time DATETIME DEFAULT NULL COMMENT '接单时间',
    pickup_time DATETIME DEFAULT NULL COMMENT '取件时间',
    delivery_time DATETIME DEFAULT NULL COMMENT '送达时间',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    images TEXT COMMENT '图片URL列表，逗号分隔',
    is_urgent TINYINT DEFAULT 0 COMMENT '是否加急：0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_runner_id (runner_id),
    INDEX idx_status (status),
    INDEX idx_task_type (task_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跑腿任务表';

-- 评价表
CREATE TABLE IF NOT EXISTS tb_evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    from_user_id BIGINT NOT NULL COMMENT '评价用户ID',
    to_user_id BIGINT NOT NULL COMMENT '被评价用户ID',
    user_type TINYINT NOT NULL COMMENT '评价者类型：1-发单用户 2-跑腿员',
    rating TINYINT NOT NULL COMMENT '评分：1-5星',
    content VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    tags VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名：0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_task_id (task_id),
    INDEX idx_from_user_id (from_user_id),
    INDEX idx_to_user_id (to_user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS tb_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    message_type TINYINT NOT NULL COMMENT '消息类型：1-系统通知 2-订单通知 3-活动通知',
    title VARCHAR(128) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    related_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
    related_type VARCHAR(32) DEFAULT NULL COMMENT '关联业务类型',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-否 1-是',
    read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_message_type (message_type),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS tb_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key VARCHAR(64) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入初始配置数据
INSERT INTO tb_config (config_key, config_value, description) VALUES
('platform_name', '校园跑腿', '平台名称'),
('platform_service_fee', '0.05', '平台服务费率'),
('min_withdraw_amount', '10.00', '最小提现金额'),
('max_task_amount', '500.00', '单笔任务最大金额'),
('auto_cancel_time', '30', '任务自动取消时间（分钟）'),
('auto_complete_time', '24', '自动确认完成时间（小时）');
