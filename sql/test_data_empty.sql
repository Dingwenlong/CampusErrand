SET NAMES utf8mb4;

USE campus_errand;

-- ========================================
-- 清空所有业务数据（保留配置、轮播图、管理员）
-- ========================================

-- 清空评价表
TRUNCATE TABLE tb_evaluation;

-- 清空任务表
TRUNCATE TABLE tb_task;

-- 清空交易流水表
TRUNCATE TABLE tb_transaction;

-- 清空消息表
TRUNCATE TABLE tb_message;

-- 清空用户钱包表（保留钱包结构）
-- 注意：不truncate，因为我们需要保留钱包记录，只是将余额设为0
UPDATE tb_user_wallet 
SET balance = 0.00, 
    frozen_amount = 0.00, 
    total_income = 0.00, 
    total_expense = 0.00;

-- 清空用户表（先清空钱包数据）
-- 先保存现有用户的钱包关系，删除用户后重新创建测试用户
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE tb_user;
TRUNCATE TABLE tb_user_wallet;
SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 创建测试用户（用于演示空状态）
-- ========================================

-- 测试用户1：发布者（无任务）
INSERT INTO tb_user (openid, nickname, avatar, phone, gender, user_type, status, is_verified, real_name, student_id, school_name, credit_score)
VALUES 
('test_openid_publisher', '测试发布者', '/static/avatar/default.png', '13800138001', 1, 0, 1, 1, '张三', '2024001', '某某大学', 100);

-- 测试用户2：跑腿员（无接单）
INSERT INTO tb_user (openid, nickname, avatar, phone, gender, user_type, status, is_verified, real_name, student_id, school_name, credit_score)
VALUES 
('test_openid_runner', '测试跑腿员', '/static/avatar/default.png', '13800138002', 1, 1, 1, 1, '李四', '2024002', '某某大学', 100);

-- 为测试用户创建钱包（余额为空）
INSERT INTO tb_user_wallet (user_id, balance, frozen_amount, total_income, total_expense)
SELECT id, 0.00, 0.00, 0.00, 0.00
FROM tb_user;

-- ========================================
-- 验证数据
-- ========================================

SELECT '数据清空完成！' AS message;
SELECT COUNT(*) AS user_count FROM tb_user;
SELECT COUNT(*) AS task_count FROM tb_task;
SELECT COUNT(*) AS evaluation_count FROM tb_evaluation;
SELECT COUNT(*) AS transaction_count FROM tb_transaction;
SELECT COUNT(*) AS message_count FROM tb_message;
