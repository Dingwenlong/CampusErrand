SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS campus_errand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_errand;

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
    verify_status TINYINT DEFAULT 0 COMMENT '认证状态：0-未认证 1-待审核 2-已认证 3-已拒绝',
    verify_reject_reason VARCHAR(255) DEFAULT NULL COMMENT '认证拒绝原因',
    id_card_front_image VARCHAR(255) DEFAULT NULL COMMENT '身份证正面照片',
    id_card_back_image VARCHAR(255) DEFAULT NULL COMMENT '身份证背面照片',
    verify_submit_time DATETIME DEFAULT NULL COMMENT '认证提交时间',
    verify_time DATETIME DEFAULT NULL COMMENT '认证审核时间',
    verify_admin_id BIGINT DEFAULT NULL COMMENT '审核管理员ID',
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

CREATE TABLE IF NOT EXISTS tb_user_wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
    frozen_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结金额',
    total_income DECIMAL(10,2) DEFAULT 0.00 COMMENT '总收入',
    total_expense DECIMAL(10,2) DEFAULT 0.00 COMMENT '总支出',
    pay_password VARCHAR(64) DEFAULT NULL COMMENT '支付密码',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';

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
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    cancel_type TINYINT DEFAULT NULL COMMENT '取消类型：1-用户取消 2-系统自动取消',
    cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_runner_id (runner_id),
    INDEX idx_status (status),
    INDEX idx_task_type (task_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跑腿任务表';

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

CREATE TABLE IF NOT EXISTS tb_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content VARCHAR(255) DEFAULT NULL COMMENT '内容描述',
    bg_color VARCHAR(255) DEFAULT NULL COMMENT '背景色',
    image_url VARCHAR(500) DEFAULT NULL COMMENT '图片URL',
    link_url VARCHAR(500) DEFAULT NULL COMMENT '链接URL',
    sort_order INT DEFAULT 0 COMMENT '排序，数字越小越靠前',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

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

-- ========== 测试数据 ==========

INSERT INTO tb_user (openid, nickname, avatar, phone, gender, user_type, status, is_verified, verify_status, real_name, id_card, student_id, school_name, credit_score, total_orders, completed_orders, cancelled_orders, create_time) VALUES
('dev_openid_1', '小明同学', 'https://example.com/avatar1.jpg', '13800138001', 1, 0, 1, 1, 2, '张明', '110101200001011234', '2020001', '北京大学', 98, 5, 4, 1, NOW()),
('dev_openid_2', '跑腿小王', 'https://example.com/avatar2.jpg', '13800138002', 1, 1, 1, 1, 2, '王跑腿', '110101200002021234', '2020002', '北京大学', 95, 12, 10, 2, NOW()),
('dev_openid_3', '测试用户', 'https://example.com/avatar3.jpg', '13800138003', 2, 0, 1, 0, 0, NULL, NULL, NULL, NULL, 80, 0, 0, 0, NOW()),
('dev_openid_4', '李同学', 'https://example.com/avatar4.jpg', '13800138004', 1, 0, 1, 1, 2, '李四', '110101200004041234', '2020004', '清华大学', 90, 3, 3, 0, NOW()),
('dev_openid_5', '赵跑腿', 'https://example.com/avatar5.jpg', '13800138005', 1, 1, 1, 1, 2, '赵六', '110101200005051234', '2020005', '清华大学', 92, 8, 7, 1, NOW());

INSERT INTO tb_user_wallet (user_id, balance, frozen_amount, total_income, total_expense, pay_password, version, create_time) VALUES
(1, 158.50, 0.00, 500.00, 341.50, NULL, 0, NOW()),
(2, 280.00, 50.00, 800.00, 520.00, NULL, 0, NOW()),
(3, 0.00, 0.00, 0.00, 0.00, NULL, 0, NOW()),
(4, 50.00, 0.00, 200.00, 150.00, NULL, 0, NOW()),
(5, 320.00, 0.00, 1000.00, 680.00, NULL, 0, NOW());

INSERT INTO tb_task (user_id, task_type, title, description, pickup_address, pickup_contact, pickup_phone, delivery_address, delivery_contact, delivery_phone, reward, weight_fee, urgency_fee, total_amount, expect_time, deadline_time, status, runner_id, accept_time, pickup_time, delivery_time, complete_time, remark, is_urgent, create_time) VALUES
(1, 1, '取快递-申通', '取一个申通快递，快递单号ST2024001', '快递站A', '小李', '13800138001', '宿舍5号楼', '小明', '13800138001', 8.00, 0.00, 0.00, 8.00, DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), 0, NULL, NULL, NULL, NULL, NULL, '快递比较大，注意轻拿轻放', 0, NOW()),
(1, 2, '送文件到教学楼', '送一份重要文件到教学楼B区，请尽快', '宿舍5号楼', '小明', '13800138001', '教学楼B区', '王老师', '13800138004', 15.00, 0.00, 5.00, 20.00, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 5, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 30 MINUTE), '文件很重要', 1, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 1, '帮忙取餐', '帮忙取外卖，三楼窗口', '食堂三楼', '外卖员', '13800138005', '宿舍3号楼', '测试用户', '13800138003', 6.00, 0.00, 0.00, 6.00, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, NOW()),
(4, 3, '送一封信', '送一封信到图书馆', '宿舍1号楼', '李同学', '13800138004', '图书馆', '管理员', '13800138006', 5.00, 0.00, 0.00, 5.00, DATE_ADD(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 5 HOUR), 1, 5, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(5, 2, '代买奶茶', '帮我买一杯珍珠奶茶，半糖少冰', '奶茶店', '赵跑腿', '13800138005', '教学楼A区', '张同学', '13800138007', 10.00, 0.00, 0.00, 10.00, DATE_ADD(NOW(), INTERVAL 30 MINUTE), DATE_ADD(NOW(), INTERVAL 1 HOUR), 3, 2, DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL, NULL, '半糖少冰', 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

INSERT INTO tb_evaluation (task_id, from_user_id, to_user_id, user_type, rating, content, tags, is_anonymous, create_time) VALUES
(2, 1, 2, 0, 5, '配送很快，态度很好！', '服务好,速度快,态度好', 0, DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(2, 2, 1, 1, 5, '准时放单，沟通顺畅', '好沟通,准时', 0, DATE_SUB(NOW(), INTERVAL 20 MINUTE));

INSERT INTO tb_transaction (transaction_no, user_id, transaction_type, direction, amount, balance, related_id, related_type, remark, status, create_time) VALUES
('TXN20240001', 1, 1, 1, 500.00, 500.00, NULL, 'recharge', '充值', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TXN20240002', 1, 3, 2, 8.00, 492.00, 1, 'task', '发布任务付款', 1, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('TXN20240003', 1, 3, 2, 20.00, 472.00, 2, 'task', '发布任务付款', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('TXN20240004', 2, 4, 1, 20.00, 280.00, 2, 'task', '任务完成收款', 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('TXN20240005', 5, 4, 1, 10.00, 320.00, 5, 'task', '任务完成收款', 1, DATE_SUB(NOW(), INTERVAL 15 MINUTE));

INSERT INTO tb_message (user_id, message_type, title, content, related_id, is_read, create_time) VALUES
(1, 2, '任务已接单', '您的任务"送文件到教学楼"已被跑腿小王接单', 2, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 2, '任务已完成', '您的任务"送文件到教学楼"已完成，记得评价', 2, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(2, 1, '有新任务', '有新的跑腿任务，快去看看', NULL, 0, NOW()),
(3, 1, '欢迎加入', '欢迎使用校园跑腿平台', NULL, 0, NOW()),
(5, 2, '任务配送中', '您接的任务"代买奶茶"正在配送中', 5, 0, DATE_SUB(NOW(), INTERVAL 10 MINUTE));

INSERT INTO tb_banner (title, content, bg_color, sort_order, status) VALUES
('校园跑腿', '便捷生活，从这里开始', 'linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%)', 1, 1),
('安全可靠', '实名认证，信用保障', 'linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%)', 2, 1),
('快速响应', '附近跑腿员，即时接单', 'linear-gradient(135deg, #667eea 0%, #8B5CF6 100%)', 3, 1);

INSERT INTO tb_config (config_key, config_value, description) VALUES
('platform_name', '校园跑腿', '平台名称'),
('platform_service_fee', '0.05', '平台服务费率'),
('min_withdraw_amount', '10.00', '最小提现金额'),
('max_task_amount', '500.00', '单笔任务最大金额'),
('auto_cancel_time', '30', '任务自动取消时间（分钟）'),
('auto_complete_time', '24', '自动确认完成时间（小时）'),
('min_reward', '1.00', '最小赏金金额'),
('max_reward', '1000.00', '最大赏金金额');
