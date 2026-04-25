SET NAMES utf8mb4;

USE campus_errand;

-- 清空表
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE tb_transaction;
TRUNCATE TABLE tb_evaluation;
TRUNCATE TABLE tb_task;
TRUNCATE TABLE tb_message;
TRUNCATE TABLE tb_user_wallet;
TRUNCATE TABLE tb_user;
TRUNCATE TABLE tb_banner;
TRUNCATE TABLE tb_config;
TRUNCATE TABLE tb_admin;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 用户数据
INSERT INTO tb_user (openid, nickname, avatar, phone, gender, user_type, status, is_verified, credit_score, total_orders, completed_orders, cancelled_orders, create_time) VALUES 
('dev_openid_1', '小明同学', 'https://example.com/avatar1.jpg', '13800138001', 1, 0, 1, 1, 98, 5, 4, 1, NOW()),
('dev_openid_2', '跑腿小王', 'https://example.com/avatar2.jpg', '13800138002', 1, 1, 1, 1, 95, 12, 10, 2, NOW()),
('dev_openid_3', '测试用户', 'https://example.com/avatar3.jpg', '13800138003', 2, 0, 1, 0, 80, 0, 0, 0, NOW());

-- 2. 用户钱包
INSERT INTO tb_user_wallet (user_id, balance, frozen_amount, total_income, total_expense, create_time, version) VALUES 
(1, 158.50, 0, 500, 341.50, NOW(), 0),
(2, 280.00, 50, 800, 520, NOW(), 0),
(3, 0.00, 0, 0, 0, NOW(), 0);

-- 3. 任务数据
INSERT INTO tb_task (user_id, task_type, title, description, pickup_address, pickup_contact, pickup_phone, delivery_address, delivery_contact, delivery_phone, reward, total_amount, status, runner_id, accept_time, pickup_time, delivery_time, complete_time, remark, is_urgent, create_time) VALUES 
(1, 1, '取快递', '取一个申通快递', '快递站A', '小李', '13800138001', '宿舍5号楼', '小明', '13800138001', 8.00, 8.00, 1, NULL, NULL, NULL, NULL, NULL, NULL, 0, NOW()),
(1, 2, '送文件', '送一份文件到教学楼', '宿舍5号楼', '小明', '13800138001', '教学楼B区', '王老师', '13800138004', 15.00, 15.00, 4, 2, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 45 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 1, '帮忙取餐', '帮忙取外卖', '食堂三楼', '外卖员', '13800138005', '宿舍3号楼', '测试用户', '13800138003', 6.00, 6.00, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NOW());

-- 4. 评价数据
INSERT INTO tb_evaluation (task_id, from_user_id, to_user_id, user_type, rating, content, tags, is_anonymous, create_time) VALUES 
(2, 1, 2, 0, 5, '配送很快，态度很好！', '服务好,速度快,态度好', 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(2, 2, 1, 1, 5, '准时放单，沟通顺畅', '好沟通,准时', 0, DATE_SUB(NOW(), INTERVAL 25 MINUTE));

-- 5. 交易记录
INSERT INTO tb_transaction (transaction_no, user_id, transaction_type, direction, amount, balance, related_id, related_type, remark, status, create_time) VALUES 
('TXN00001', 1, 1, 0, 500.00, 500.00, NULL, 'recharge', '充值', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TXN00002', 1, 2, 1, 8.00, 492.00, 1, 'task', '发布任务付款', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('TXN00003', 1, 2, 1, 15.00, 477.00, 2, 'task', '发布任务付款', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('TXN00004', 2, 3, 0, 15.00, 280.00, 2, 'task', '任务完成收款', 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- 6. 消息数据
INSERT INTO tb_message (user_id, message_type, title, content, related_id, is_read, create_time) VALUES 
(1, 1, '任务已接单', '您的任务已被跑腿小王接单', 2, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 2, '任务已完成', '您的送文件任务已完成，记得评价', 2, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(2, 1, '有新任务', '有新的跑腿任务，快去看看', NULL, 0, NOW());

-- 7. 轮播图
INSERT INTO tb_banner (title, content, bg_color, image_url, link_url, sort_order, status, create_time) VALUES 
('开学季活动', '新用户首单立减5元', '#FF6B6B', 'https://example.com/banner1.jpg', '', 1, 1, NOW()),
('邀请有礼', '邀请好友下单得红包', '#4ECDC4', 'https://example.com/banner2.jpg', '', 2, 1, NOW());

-- 8. 配置数据
INSERT INTO tb_config (config_key, config_value, description, create_time) VALUES 
('platform_fee_rate', '0.1', '平台手续费比例', NOW()),
('min_reward', '2', '最低任务金额', NOW()),
('max_reward', '500', '最高任务金额', NOW());

-- 9. 管理员账号
INSERT INTO tb_admin (username, password_hash, nickname, role, status, force_password_change, create_time) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin', 1, 0, NOW()),
('manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '运营经理', 'manager', 1, 0, NOW());
