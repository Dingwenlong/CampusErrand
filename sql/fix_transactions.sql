SET NAMES utf8mb4;

USE campus_errand;

-- 先删除刚才的记录
DELETE FROM tb_transaction WHERE user_id = 4;

-- 添加初始充值记录（给用户4充值2000元）
INSERT INTO tb_transaction (
  transaction_no, user_id, transaction_type, direction,
  amount, balance, related_id, related_type, remark, status, create_time
) VALUES (
  'TXN_202504260001', 4, 1, 1, 2000.00, 2000.00, NULL, 'recharge', '虚拟充值2000元', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)
);

-- 添加任务收入记录
INSERT INTO tb_transaction (
  transaction_no, user_id, transaction_type, direction,
  amount, balance, related_id, related_type, remark, status, create_time
) VALUES (
  'TXN_202504260002', 4, 4, 1, 12.00, 2012.00, 7, 'task', '完成买奶茶任务奖励', 1, DATE_SUB(NOW(), INTERVAL 22 HOUR)
);

-- 添加平台手续费记录
INSERT INTO tb_transaction (
  transaction_no, user_id, transaction_type, direction,
  amount, balance, related_id, related_type, remark, status, create_time
) VALUES (
  'TXN_202504260003', 4, 2, 2, 1.20, 2010.80, 7, 'platform_fee', '平台手续费10%', 1, DATE_SUB(NOW(), INTERVAL 22 HOUR)
);
