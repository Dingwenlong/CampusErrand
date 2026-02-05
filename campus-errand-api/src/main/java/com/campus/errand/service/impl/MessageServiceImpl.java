package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.entity.Message;
import com.campus.errand.mapper.MessageMapper;
import com.campus.errand.service.MessageService;
import com.campus.errand.vo.MessageVO;
import com.campus.errand.websocket.WebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final ObjectMapper objectMapper;

    @Autowired
    public MessageServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Message sendMessage(Long userId, Integer type, String title, String content, Long relatedId) {
        Message message = new Message();
        message.setUserId(userId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setRelatedId(relatedId);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());

        save(message);

        // WebSocket推送
        pushMessageToUser(userId, message);

        return message;
    }

    private void pushMessageToUser(Long userId, Message message) {
        try {
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "message");
            pushData.put("data", convertToVO(message));

            String jsonMessage = objectMapper.writeValueAsString(pushData);
            WebSocketHandler.sendMessageToUser(userId, jsonMessage);
        } catch (Exception e) {
            System.out.println("WebSocket推送消息失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<MessageVO> getUserMessages(Long userId, Long current, Long size) {
        Page<Message> page = new Page<>(current, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId)
               .orderByDesc(Message::getCreateTime);

        IPage<Message> messagePage = page(page, wrapper);

        Page<MessageVO> voPage = new Page<>();
        voPage.setCurrent(messagePage.getCurrent());
        voPage.setSize(messagePage.getSize());
        voPage.setTotal(messagePage.getTotal());
        voPage.setRecords(messagePage.getRecords().stream()
                .map(this::convertToVO)
                .toList());

        return voPage;
    }

    @Override
    public boolean markAsRead(Long messageId, Long userId) {
        Message message = getById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            return false;
        }

        message.setIsRead(1);
        return updateById(message);
    }

    @Override
    public int markAllAsRead(Long userId) {
        return baseMapper.markAllAsRead(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return baseMapper.countUnreadByUserId(userId);
    }

    @Override
    public void sendTaskAcceptedNotification(Long publisherId, Long taskId, String taskTitle, String runnerName) {
        sendMessage(
            publisherId,
            1,
            "任务被接单",
            runnerName + " 接取了您的任务：" + taskTitle,
            taskId
        );
    }

    @Override
    public void sendDeliveredNotification(Long publisherId, Long taskId, String taskTitle) {
        sendMessage(
            publisherId,
            4,
            "跑腿员已送达",
            "您的任务 " + taskTitle + " 已送达，请确认收货",
            taskId
        );
    }

    @Override
    public void sendReceivedNotification(Long runnerId, Long taskId, String taskTitle) {
        sendMessage(
            runnerId,
            5,
            "任务已完成",
            "发单者已确认收货，任务 " + taskTitle + " 已完成，报酬已到账",
            taskId
        );
    }

    @Override
    public void sendOrderCancelledNotification(Long userId, Long taskId, String taskTitle, boolean isPublisher) {
        String content = isPublisher
            ? "您的任务 " + taskTitle + " 已被取消"
            : "您接取的任务 " + taskTitle + " 已被发单者取消";

        sendMessage(
            userId,
            3,
            "订单已取消",
            content,
            taskId
        );
    }

    @Override
    public void sendEvaluationNotification(Long userId, Long taskId, String taskTitle, Integer rating) {
        sendMessage(
            userId,
            6,
            "收到新评价",
            "您收到了 " + rating + " 星评价，任务：" + taskTitle,
            taskId
        );
    }

    @Override
    public void sendBalanceChangeNotification(Long userId, String amount, String balance, String type) {
        String title = "资金变动";
        String content = type + "：" + amount + "，当前余额：" + balance;

        sendMessage(
            userId,
            7,
            title,
            content,
            null
        );
    }

    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setTypeName(getTypeName(message.getType()));
        return vo;
    }

    private String getTypeName(Integer type) {
        switch (type) {
            case 1: return "任务被接单";
            case 2: return "任务状态变更";
            case 3: return "订单取消";
            case 4: return "确认送达";
            case 5: return "确认收货";
            case 6: return "收到评价";
            case 7: return "资金变动";
            case 8: return "系统通知";
            default: return "其他";
        }
    }
}
