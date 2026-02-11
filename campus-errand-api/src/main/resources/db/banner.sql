-- 轮播图表
CREATE TABLE IF NOT EXISTS tb_banner (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(100) NOT NULL COMMENT '标题',
  content VARCHAR(255) DEFAULT NULL COMMENT '内容描述',
  bg_color VARCHAR(255) DEFAULT NULL COMMENT '背景色',
  image_url VARCHAR(500) DEFAULT NULL COMMENT '图片URL',
  link_url VARCHAR(500) DEFAULT NULL COMMENT '链接URL',
  sort_order INT DEFAULT 0 COMMENT '排序，数字越小越靠前',
  status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 初始化默认轮播图数据
INSERT INTO tb_banner (title, content, bg_color, sort_order, status) VALUES
('校园跑腿', '便捷生活，从这里开始', 'linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%)', 1, 1),
('安全可靠', '实名认证，信用保障', 'linear-gradient(135deg, #4ECDC4 0%, #7EDDD6 100%)', 2, 1),
('快速响应', '附近跑腿员，即时接单', 'linear-gradient(135deg, #667eea 0%, #8B5CF6 100%)', 3, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), bg_color = VALUES(bg_color);
