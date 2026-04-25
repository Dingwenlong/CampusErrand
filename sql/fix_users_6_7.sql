SET NAMES utf8mb4;

USE campus_errand;

-- 为用户6和7创建钱包
INSERT IGNORE INTO tb_user_wallet (user_id, balance, frozen_amount, total_income, total_expense, pay_password, version, create_time, update_time, deleted)
VALUES 
(6, 2010.80, 0.00, 2012.00, 1.20, NULL, 0, NOW(), NOW(), 0),
(7, 2010.80, 0.00, 2012.00, 1.20, NULL, 0, NOW(), NOW(), 0);

-- 为用户6和7添加交易记录
INSERT IGNORE INTO tb_transaction (transaction_no, user_id, transaction_type, direction, amount, balance, related_id, related_type, remark, status, create_time)
VALUES
('TXN_20250426_USER6_1', 6, 1, 1, 2000.00, 2000.00, NULL, '充值', '虚拟充值2000元', 1, NOW()),
('TXN_20250426_USER6_2', 6, 4, 1, 12.00, 2012.00, NULL, 'task', '完成任务奖励', 1, NOW()),
('TXN_20250426_USER6_3', 6, 2, 2, 1.20, 2010.80, NULL, 'platform_fee', '平台手续费', 1, NOW()),
('TXN_20250426_USER7_1', 7, 1, 1, 2000.00, 2000.00, NULL, '充值', '虚拟充值2000元', 1, NOW()),
('TXN_20250426_USER7_2', 7, 4, 1, 12.00, 2012.00, NULL, 'task', '完成任务奖励', 1, NOW()),
('TXN_20250426_USER7_3', 7, 2, 2, 1.20, 2010.80, NULL, 'platform_fee', '平台手续费', 1, NOW());

-- 为用户6和7添加接单任务
INSERT IGNORE INTO tb_task (
  user_id, task_type, title, description, 
  pickup_address, pickup_contact, pickup_phone,
  delivery_address, delivery_contact, delivery_phone,
  reward, weight_fee, urgency_fee, total_amount,
  status, runner_id, accept_time, complete_time, remark, is_urgent, create_time
)
VALUES 
(1, 1, '取快递', '帮我取个圆通快递，送到宿舍楼下', '快递驿站A', '快递员', '13800138000', '宿舍5号楼', '小明', '13800138001', 8.00, 0, 0, 8.00, 2, 6, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, '尽快送来，谢谢', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 2, '送文件', '把这份文件送到教学楼', '宿舍2号楼', '小王', '13800138002', '教学楼B区', '李老师', '13800138003', 15.00, 2, 3, 20.00, 3, 6, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, '文件很重要，小心点', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 3, '买奶茶', '帮我买一杯珍珠奶茶，加冰，谢谢', '食堂三楼', '奶茶店', '13800138004', '图书馆门口', '小明', '13800138001', 12.00, 0, 0, 12.00, 4, 6, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 22 HOUR), '顾客好评', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 1, '取快递', '帮我取个圆通快递，送到宿舍楼下', '快递驿站A', '快递员', '13800138000', '宿舍5号楼', '小明', '13800138001', 8.00, 0, 0, 8.00, 2, 7, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, '尽快送来，谢谢', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 2, '送文件', '把这份文件送到教学楼', '宿舍2号楼', '小王', '13800138002', '教学楼B区', '李老师', '13800138003', 15.00, 2, 3, 20.00, 3, 7, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, '文件很重要，小心点', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 3, '买奶茶', '帮我买一杯珍珠奶茶，加冰，谢谢', '食堂三楼', '奶茶店', '13800138004', '图书馆门口', '小明', '13800138001', 12.00, 0, 0, 12.00, 4, 7, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 22 HOUR), '顾客好评', 0, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 更新用户6和7的统计数据
UPDATE tb_user SET total_orders = 3, completed_orders = 1 WHERE id IN (6, 7);
