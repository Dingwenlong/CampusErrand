-- 系统配置表
CREATE TABLE IF NOT EXISTS tb_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  config_key VARCHAR(100) NOT NULL COMMENT '配置键',
  config_value VARCHAR(500) DEFAULT NULL COMMENT '配置值',
  description VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
  category VARCHAR(50) DEFAULT 'other' COMMENT '配置分类',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 基础配置
INSERT INTO tb_config (config_key, config_value, description, category) VALUES
('site.name', '校园跑腿', '系统名称', 'basic'),
('site.logo', '', '系统Logo URL', 'basic'),
('service.phone', '400-123-4567', '客服电话', 'basic'),
('service.email', 'service@campus.com', '客服邮箱', 'basic');

-- 任务配置
INSERT INTO tb_config (config_key, config_value, description, category) VALUES
('task.min.reward', '1.00', '最小赏金金额', 'task'),
('task.max.reward', '1000.00', '最大赏金金额', 'task'),
('task.auto.cancel.hours', '24', '任务自动取消时间（小时）', 'task'),
('task.timeout.warning.hours', '2', '任务超时提醒时间（小时）', 'task');

-- 钱包配置
INSERT INTO tb_config (config_key, config_value, description, category) VALUES
('wallet.min.withdraw', '1.00', '最低提现金额', 'wallet'),
('wallet.max.withdraw', '5000.00', '最高提现金额', 'wallet'),
('wallet.platform.fee', '0.00', '平台抽成比例（%）', 'wallet');

-- 其他配置
INSERT INTO tb_config (config_key, config_value, description, category) VALUES
('user.register.enabled', 'true', '用户注册开关', 'other'),
('task.publish.enabled', 'true', '任务发布开关', 'other'),
('user.verify.enabled', 'false', '实名认证开关', 'other'),
('system.maintenance.mode', 'false', '系统维护开关', 'other'),
('system.maintenance.message', '系统维护中，请稍后再试', '维护提示信息', 'other');
