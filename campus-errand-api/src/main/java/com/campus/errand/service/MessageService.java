package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.Message;
import com.campus.errand.vo.MessageVO;

public interface MessageService extends IService<Message> {

    /**
     * 发送消息
     * @param userId 接收用户ID
     * @param type 消息类型
     * @param title 标题
     * @param content 内容
     * @param relatedId 关联ID
     * @return 消息对象
     */
    Message sendMessage(Long userId, Integer type, String title, String content, Long relatedId);

    /**
     * 获取用户消息列表
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页消息列表
     */
    IPage<MessageVO> getUserMessages(Long userId, Long current, Long size);

    /**
     * 标记消息已读
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long messageId, Long userId);

    /**
     * 标记所有消息已读
     * @param userId 用户ID
     * @return 已读数量
     */
    int markAllAsRead(Long userId);

    /**
     * 获取未读消息数
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadCount(Long userId);

    /**
     * 发送任务被接单通知
     * @param publisherId 发单者ID
     * @param taskId 任务ID
     * @param taskTitle 任务标题
     * @param runnerName 跑腿员名称
     */
    void sendTaskAcceptedNotification(Long publisherId, Long taskId, String taskTitle, String runnerName);

    /**
     * 发送确认送达通知
     * @param publisherId 发单者ID
     * @param taskId 任务ID
     * @param taskTitle 任务标题
     */
    void sendDeliveredNotification(Long publisherId, Long taskId, String taskTitle);

    /**
     * 发送确认收货通知
     * @param runnerId 跑腿员ID
     * @param taskId 任务ID
     * @param taskTitle 任务标题
     */
    void sendReceivedNotification(Long runnerId, Long taskId, String taskTitle);

    /**
     * 发送订单取消通知
     * @param userId 接收用户ID
     * @param taskId 任务ID
     * @param taskTitle 任务标题
     * @param isPublisher 是否是发单者
     */
    void sendOrderCancelledNotification(Long userId, Long taskId, String taskTitle, boolean isPublisher);

    /**
     * 发送收到评价通知
     * @param userId 被评价用户ID
     * @param taskId 任务ID
     * @param taskTitle 任务标题
     * @param rating 评分
     */
    void sendEvaluationNotification(Long userId, Long taskId, String taskTitle, Integer rating);

    /**
     * 发送资金变动通知
     * @param userId 用户ID
     * @param amount 变动金额
     * @param balance 当前余额
     * @param type 变动类型
     */
    void sendBalanceChangeNotification(Long userId, String amount, String balance, String type);
}
