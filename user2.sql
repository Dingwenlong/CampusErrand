USE campus_errand;

INSERT INTO tb_user (id, openid, nickname, phone, gender, status, is_verified, student_id, credit_score) 
VALUES (2, 'mock_user2_openid', '用户二', '13800000002', 1, 1, 1, '20240002', 100) 
ON DUPLICATE KEY UPDATE openid = 'mock_user2_openid', nickname = '用户二', status = 1;

INSERT INTO tb_user_wallet (user_id, balance, frozen_amount, total_income, total_expense) 
VALUES (2, 100.00, 0.00, 0.00, 0.00) 
ON DUPLICATE KEY UPDATE balance = 100.00;
