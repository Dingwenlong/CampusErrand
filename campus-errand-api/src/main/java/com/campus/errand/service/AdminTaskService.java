package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.vo.TaskVO;

import java.util.Map;

public interface AdminTaskService {

    /**
     * 获取任务列表
     */
    IPage<TaskVO> getTaskList(Integer taskType, Integer status, String keyword, Long current, Long size);

    /**
     * 获取任务详情
     */
    TaskVO getTaskDetail(Long taskId);

    /**
     * 取消任务
     */
    boolean cancelTask(Long taskId, String reason);

    /**
     * 删除任务
     */
    boolean deleteTask(Long taskId);

    /**
     * 获取任务统计
     */
    Map<String, Object> getTaskStats();
}
