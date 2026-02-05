package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.dto.TaskPublishDTO;
import com.campus.errand.entity.Task;
import com.campus.errand.vo.TaskVO;

public interface TaskService extends IService<Task> {

    TaskVO publishTask(Long userId, TaskPublishDTO publishDTO);

    IPage<TaskVO> getTaskList(Integer taskType, Integer status, Long current, Long size);

    TaskVO getTaskDetail(Long taskId);

    boolean acceptTask(Long taskId, Long runnerId);

    boolean updateTaskStatus(Long taskId, Integer status, Long userId);
}
