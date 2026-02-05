package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.dto.TaskPublishDTO;
import com.campus.errand.entity.Task;
import com.campus.errand.entity.User;
import com.campus.errand.mapper.TaskMapper;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.service.TaskService;
import com.campus.errand.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskVO publishTask(Long userId, TaskPublishDTO publishDTO) {
        Task task = new Task();
        BeanUtils.copyProperties(publishDTO, task);
        task.setUserId(userId);
        
        // 计算总金额
        BigDecimal totalAmount = publishDTO.getReward();
        if (publishDTO.getWeightFee() != null) {
            totalAmount = totalAmount.add(publishDTO.getWeightFee());
        }
        if (publishDTO.getUrgencyFee() != null) {
            totalAmount = totalAmount.add(publishDTO.getUrgencyFee());
        }
        task.setTotalAmount(totalAmount);
        
        // 设置初始状态
        task.setStatus(0);
        
        save(task);
        
        return convertToVO(task);
    }

    @Override
    public IPage<TaskVO> getTaskList(Integer taskType, Integer status, Long current, Long size) {
        Page<Task> page = new Page<>(current, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        
        // 默认查询待接单的任务
        if (status == null) {
            status = 0;
        }
        wrapper.eq(Task::getStatus, status);
        
        if (taskType != null) {
            wrapper.eq(Task::getTaskType, taskType);
        }
        
        wrapper.orderByDesc(Task::getCreateTime);
        
        IPage<Task> taskPage = page(page, wrapper);
        
        List<TaskVO> voList = taskPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Page<TaskVO> voPage = new Page<>();
        voPage.setCurrent(taskPage.getCurrent());
        voPage.setSize(taskPage.getSize());
        voPage.setTotal(taskPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public TaskVO getTaskDetail(Long taskId) {
        Task task = getById(taskId);
        if (task == null) {
            return null;
        }
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptTask(Long taskId, Long runnerId) {
        String lockKey = "task:lock:" + taskId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        
        if (!Boolean.TRUE.equals(locked)) {
            return false;
        }
        
        try {
            Task task = getById(taskId);
            if (task == null || task.getStatus() != 0) {
                return false;
            }
            
            task.setRunnerId(runnerId);
            task.setStatus(1);
            task.setAcceptTime(LocalDateTime.now());
            
            return updateById(task);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTaskStatus(Long taskId, Integer status, Long userId) {
        Task task = getById(taskId);
        if (task == null) {
            return false;
        }
        
        // 验证权限
        if (!task.getUserId().equals(userId) && !task.getRunnerId().equals(userId)) {
            return false;
        }
        
        // 验证状态流转是否合法
        if (!isValidStatusTransition(task.getStatus(), status)) {
            return false;
        }
        
        task.setStatus(status);
        
        // 更新相应的时间字段
        switch (status) {
            case 2: // 待取件
                task.setPickupTime(LocalDateTime.now());
                break;
            case 3: // 配送中
                break;
            case 4: // 待确认
                task.setDeliveryTime(LocalDateTime.now());
                break;
            case 5: // 已完成
                task.setCompleteTime(LocalDateTime.now());
                break;
        }
        
        return updateById(task);
    }

    private boolean isValidStatusTransition(Integer currentStatus, Integer newStatus) {
        // 状态流转规则
        switch (currentStatus) {
            case 0: // 待接单
                return newStatus == 1 || newStatus == 6;
            case 1: // 已接单
                return newStatus == 2 || newStatus == 6;
            case 2: // 待取件
                return newStatus == 3 || newStatus == 6;
            case 3: // 配送中
                return newStatus == 4;
            case 4: // 待确认
                return newStatus == 5;
            default:
                return false;
        }
    }

    private TaskVO convertToVO(Task task) {
        TaskVO vo = new TaskVO();
        BeanUtils.copyProperties(task, vo);
        
        // 设置任务类型名称
        vo.setTaskTypeName(getTaskTypeName(task.getTaskType()));
        
        // 设置状态名称
        vo.setStatusName(getStatusName(task.getStatus()));
        
        // 查询发布者信息
        User publisher = userMapper.selectById(task.getUserId());
        if (publisher != null) {
            vo.setPublisherName(publisher.getNickname());
            vo.setPublisherAvatar(publisher.getAvatar());
            vo.setPublisherCreditScore(publisher.getCreditScore());
        }
        
        // 查询接单者信息
        if (task.getRunnerId() != null) {
            User runner = userMapper.selectById(task.getRunnerId());
            if (runner != null) {
                vo.setRunnerName(runner.getNickname());
                vo.setRunnerAvatar(runner.getAvatar());
            }
        }
        
        return vo;
    }

    private String getTaskTypeName(Integer taskType) {
        switch (taskType) {
            case 1: return "取快递";
            case 2: return "代买";
            case 3: return "送件";
            case 4: return "其他";
            default: return "未知";
        }
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "待接单";
            case 1: return "已接单";
            case 2: return "待取件";
            case 3: return "配送中";
            case 4: return "待确认";
            case 5: return "已完成";
            case 6: return "已取消";
            default: return "未知";
        }
    }
}
