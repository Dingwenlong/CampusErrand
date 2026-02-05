-- 消息表
CREATE TABLE IF NOT EXISTS tb_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type INT NOT NULL COMMENT '消息类型：1-任务被接单 2-任务状态变更 3-订单取消 4-确认送达 5-确认收货 6-收到评价 7-资金变动 8-系统通知',
    title VARCHAR(100) COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    related_id BIGINT COMMENT '关联ID（任务ID等）',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
