SET NAMES utf8mb4;

USE campus_errand;

-- 更新 tb_user 表，添加缺失的字段
ALTER TABLE tb_user 
ADD COLUMN verify_status TINYINT DEFAULT 0 COMMENT '认证状态：0-未认证 1-待审核 2-已认证 3-已拒绝';

ALTER TABLE tb_user 
ADD COLUMN verify_reject_reason VARCHAR(255) DEFAULT NULL COMMENT '认证拒绝原因';

ALTER TABLE tb_user 
ADD COLUMN id_card_front_image VARCHAR(255) DEFAULT NULL COMMENT '身份证正面照片';

ALTER TABLE tb_user 
ADD COLUMN id_card_back_image VARCHAR(255) DEFAULT NULL COMMENT '身份证背面照片';

ALTER TABLE tb_user 
ADD COLUMN verify_submit_time DATETIME DEFAULT NULL COMMENT '认证提交时间';

ALTER TABLE tb_user 
ADD COLUMN verify_time DATETIME DEFAULT NULL COMMENT '认证审核时间';

ALTER TABLE tb_user 
ADD COLUMN verify_admin_id BIGINT DEFAULT NULL COMMENT '审核管理员ID';
