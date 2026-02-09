package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.dto.TaskCancelDTO;
import com.campus.errand.dto.TaskPublishDTO;
import com.campus.errand.entity.Task;
import com.campus.errand.vo.TaskVO;

public interface TaskService extends IService<Task> {

    TaskVO publishTask(Long userId, TaskPublishDTO publishDTO);

    IPage<TaskVO> getTaskList(Integer taskType, Integer status, Long current, Long size);

    TaskVO getTaskDetail(Long taskId);

    boolean acceptTask(Long taskId, Long runnerId);

    boolean updateTaskStatus(Long taskId, Integer status, Long userId);

    /**
     * 取消任务
     * @param taskId 任务ID
     * @param userId 操作用户ID
     * @param cancelDTO 取消信息
     * @return 是否成功
     */
    boolean cancelTask(Long taskId, Long userId, TaskCancelDTO cancelDTO);

    /**
     * 跑腿员确认送达
     * @param taskId 任务ID
     * @param runnerId 跑腿员ID
     * @return 是否成功
     */
    boolean deliverTask(Long taskId, Long runnerId);

    /**
     * 发单者确认收货
     * @param taskId 任务ID
     * @param userId 发单者ID
     * @return 是否成功
     */
    boolean receiveTask(Long taskId, Long userId);

    /**
     * 获取我的任务列表
     * @param userId 用户ID
     * @param role 角色：1-发单者 2-接单者
     * @param status 状态筛选
     * @param current 当前页
     * @param size 每页大小
     * @return 任务列表
     */
    IPage<TaskVO> getMyTasks(Long userId, Integer role, Integer status, Long current, Long size);
}
