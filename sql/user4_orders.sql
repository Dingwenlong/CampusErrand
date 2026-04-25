SET NAMES utf8mb4;

USE campus_errand;

-- 为用户4创建一些我接单的任务数据
INSERT INTO tb_task (
  user_id, task_type, title, description, 
  pickup_address, pickup_contact, pickup_phone,
  delivery_address, delivery_contact, delivery_phone,
  reward, weight_fee, urgency_fee, total_amount,
  status, runner_id, accept_time, complete_time, remark, is_urgent, create_time
) VALUES 
-- 进行中任务：待取件
(
  1, 1, '取快递', '帮我取个圆通快递，送到宿舍楼下',
  '快递驿站A', '快递员', '13800138000',
  '宿舍5号楼', '小明', '13800138001',
  8.00, 0, 0, 8.00,
  2, 4, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, '尽快送来，谢谢', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)
),
-- 进行中任务：配送中
(
  2, 2, '送文件', '把这份文件送到教学楼',
  '宿舍2号楼', '小王', '13800138002',
  '教学楼B区', '李老师', '13800138003',
  15.00, 2, 3, 20.00,
  3, 4, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, '文件很重要，小心点', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)
),
-- 已完成任务
(
  1, 3, '买奶茶', '帮我买一杯珍珠奶茶，加冰，谢谢',
  '食堂三楼', '奶茶店', '13800138004',
  '图书馆门口', '小明', '13800138001',
  12.00, 0, 0, 12.00,
  4, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 22 HOUR), '顾客好评', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)
),
-- 已取消任务
(
  3, 1, '帮取快递', '取个中通快递',
  '快递站B', '快递员', '13800138005',
  '宿舍3号楼', '测试用户', '13800138003',
  6.00, 0, 0, 6.00,
  5, 4, DATE_SUB(NOW(), INTERVAL 3 HOUR), NULL, '用户取消了，不用取了', 0, DATE_SUB(NOW(), INTERVAL 4 HOUR)
);

-- 更新用户4的统计数据
UPDATE tb_user SET total_orders = 4, completed_orders = 1, cancelled_orders = 1 WHERE id = 4;

-- 添加几条评价数据
INSERT INTO tb_evaluation (task_id, from_user_id, to_user_id, user_type, rating, content, tags, is_anonymous, create_time) VALUES 
(7, 1, 4, 1, 5, '配送速度很快，态度很好！非常感谢！', '服务好,速度快,态度好', 0, DATE_SUB(NOW(), INTERVAL 22 HOUR)),
(7, 4, 1, 0, 5, '下单准时，沟通顺利，非常棒的用户！', '好沟通,准时', 0, DATE_SUB(NOW(), INTERVAL 21 HOUR));

-- 添加交易记录
INSERT INTO tb_transaction (
  transaction_no, user_id, transaction_type, direction, 
  amount, balance, related_id, related_type, remark, status, create_time
) VALUES 
-- 用户4的收入记录
(
  'TXN_ORDER_001', 4, 3, 0, 12.00, 2012.00, 
  7, 'task', '完成买奶茶任务奖励', 1, DATE_SUB(NOW(), INTERVAL 22 HOUR)
),
-- 平台手续费
(
  'TXN_FEE_001', 4, 2, 1, 1.20, 2010.80, 
  7, 'platform_fee', '平台手续费10%', 1, DATE_SUB(NOW(), INTERVAL 22 HOUR)
);

-- 更新用户4钱包（2000 + 12 - 1.2 = 2010.8）
UPDATE tb_user_wallet SET balance = 2010.80, total_income = 2012.00, total_expense = 1.20 WHERE user_id = 4;
