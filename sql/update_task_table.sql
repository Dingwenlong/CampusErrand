-- 更新任务表，添加取消相关字段
ALTER TABLE tb_task
ADD COLUMN cancel_time DATETIME DEFAULT NULL COMMENT '取消时间' AFTER is_urgent,
ADD COLUMN cancel_type TINYINT DEFAULT NULL COMMENT '取消类型：1-用户取消 2-系统自动取消' AFTER cancel_time,
ADD COLUMN cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因' AFTER cancel_type;
