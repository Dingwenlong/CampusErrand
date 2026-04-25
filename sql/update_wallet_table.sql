SET NAMES utf8mb4;

USE campus_errand;

-- 更新 tb_user_wallet 表，添加缺失的字段
ALTER TABLE tb_user_wallet 
ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号';
