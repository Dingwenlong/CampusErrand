package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.Task;
import com.campus.errand.entity.User;
import com.campus.errand.mapper.TaskMapper;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.service.AdminTaskService;
import com.campus.errand.vo.TaskVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminTaskServiceImpl implements AdminTaskService {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    @Autowired
    public AdminTaskServiceImpl(TaskMapper taskMapper, UserMapper userMapper) {
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
    }

    @Override
    public IPage<TaskVO> getTaskList(Integer taskType, Integer status, String keyword, Long current, Long size) {
        Page<Task> page = new Page<>(current, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        // 任务类型筛选
        if (taskType != null) {
            wrapper.eq(Task::getTaskType, taskType);
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        // 关键词搜索（标题）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Task::getTitle, keyword.trim());
        }

        wrapper.orderByDesc(Task::getCreateTime);

        IPage<Task> taskPage = taskMapper.selectPage(page, wrapper);

        // 转换为VO
        List<TaskVO> voList = taskPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<TaskVO> resultPage = new Page<>();
        resultPage.setCurrent(taskPage.getCurrent());
        resultPage.setSize(taskPage.getSize());
        resultPage.setTotal(taskPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public TaskVO getTaskDetail(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTask(Long taskId, String reason) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }

        // 只有待接单、已接单、待取件的任务可以取消
        Integer status = task.getStatus();
        if (status != 0 && status != 1 && status != 2) {
            return false;
        }

        // 更新任务状态为已取消
        task.setStatus(6);
        task.setCancelTime(LocalDateTime.now());
        task.setCancelType(3); // 3-管理员取消
        task.setCancelReason(reason);

        return taskMapper.updateById(task) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTask(Long taskId) {
        // 只能删除已取消的任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }

        if (task.getStatus() != 6) {
            return false;
        }

        return taskMapper.deleteById(taskId) > 0;
    }

    @Override
    public Map<String, Object> getTaskStats() {
        Map<String, Object> result = new HashMap<>();

        // 总任务数
        Long totalCount = taskMapper.selectCount(null);
        result.put("totalCount", totalCount);

        // 各状态任务数量
        int[] statusArray = {0, 1, 2, 3, 4, 5, 6};
        String[] statusNames = {"待接单", "已接单", "待取件", "配送中", "待确认", "已完成", "已取消"};

        Map<String, Long> statusCount = new HashMap<>();
        for (int i = 0; i < statusArray.length; i++) {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getStatus, statusArray[i]);
            Long count = taskMapper.selectCount(wrapper);
            statusCount.put(statusNames[i], count);
        }
        result.put("statusCount", statusCount);

        // 今日新增任务
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LambdaQueryWrapper<Task> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Task::getCreateTime, todayStart)
                   .lt(Task::getCreateTime, todayEnd);
        Long todayCount = taskMapper.selectCount(todayWrapper);
        result.put("todayCount", todayCount);

        // 任务类型分布
        int[] typeArray = {1, 2, 3, 4};
        String[] typeNames = {"取快递", "代买", "送件", "其他"};

        Map<String, Long> typeCount = new HashMap<>();
        for (int i = 0; i < typeArray.length; i++) {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getTaskType, typeArray[i]);
            Long count = taskMapper.selectCount(wrapper);
            typeCount.put(typeNames[i], count);
        }
        result.put("typeCount", typeCount);

        return result;
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
            vo.setPublisherPhone(publisher.getPhone());
        }

        // 查询接单者信息
        if (task.getRunnerId() != null) {
            User runner = userMapper.selectById(task.getRunnerId());
            if (runner != null) {
                vo.setRunnerName(runner.getNickname());
                vo.setRunnerAvatar(runner.getAvatar());
                vo.setRunnerPhone(runner.getPhone());
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
