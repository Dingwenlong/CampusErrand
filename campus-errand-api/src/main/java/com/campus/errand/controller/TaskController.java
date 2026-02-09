package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.common.PageResult;
import com.campus.errand.common.Result;
import com.campus.errand.dto.TaskCancelDTO;
import com.campus.errand.dto.TaskPublishDTO;
import com.campus.errand.service.TaskService;
import com.campus.errand.util.UserContext;
import com.campus.errand.vo.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "任务管理", description = "跑腿任务相关接口")
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "发布任务")
    @PostMapping("/publish")
    public Result<TaskVO> publish(@Valid @RequestBody TaskPublishDTO publishDTO) {
        Long userId = UserContext.getUserId();
        TaskVO taskVO = taskService.publishTask(userId, publishDTO);
        return Result.success(taskVO);
    }

    @Operation(summary = "获取任务列表")
    @GetMapping("/list")
    public Result<PageResult<TaskVO>> list(
            @Parameter(description = "任务类型：1-取快递 2-代买 3-送件 4-其他") @RequestParam(required = false) Integer taskType,
            @Parameter(description = "任务状态：0-待接单 1-已接单 2-待取件 3-配送中 4-待确认 5-已完成 6-已取消") @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<TaskVO> page = taskService.getTaskList(taskType, status, current, size);
        PageResult<TaskVO> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return Result.success(result);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<TaskVO> detail(@PathVariable Long id) {
        TaskVO taskVO = taskService.getTaskDetail(id);
        if (taskVO == null) {
            return Result.error("任务不存在");
        }
        return Result.success(taskVO);
    }

    @Operation(summary = "抢单/接单")
    @PostMapping("/{id}/accept")
    public Result<Boolean> accept(@PathVariable Long id) {
        Long runnerId = UserContext.getUserId();
        boolean success = taskService.acceptTask(id, runnerId);
        if (!success) {
            return Result.error("抢单失败，任务可能已被接单");
        }
        return Result.success(true);
    }

    @Operation(summary = "更新任务状态")
    @PostMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态：0-待接单 1-已接单 2-待取件 3-配送中 4-待确认 5-已完成 6-已取消") @RequestParam Integer status) {
        Long userId = UserContext.getUserId();
        boolean success = taskService.updateTaskStatus(id, status, userId);
        if (!success) {
            return Result.error("状态更新失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancel(
            @PathVariable Long id,
            @Valid @RequestBody TaskCancelDTO cancelDTO) {
        Long userId = UserContext.getUserId();
        boolean success = taskService.cancelTask(id, userId, cancelDTO);
        if (!success) {
            return Result.error("取消任务失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "确认送达（跑腿员）")
    @PostMapping("/{id}/deliver")
    public Result<Boolean> deliver(@PathVariable Long id) {
        Long runnerId = UserContext.getUserId();
        try {
            boolean success = taskService.deliverTask(id, runnerId);
            if (!success) {
                return Result.error("确认送达失败");
            }
            return Result.success(true);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "确认收货（发单者）")
    @PostMapping("/{id}/receive")
    public Result<Boolean> receive(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        try {
            boolean success = taskService.receiveTask(id, userId);
            if (!success) {
                return Result.error("确认收货失败");
            }
            return Result.success(true);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的任务列表")
    @GetMapping("/my-tasks")
    public Result<PageResult<TaskVO>> getMyTasks(
            @Parameter(description = "角色：1-发单者 2-接单者") @RequestParam Integer role,
            @Parameter(description = "任务状态：0-待接单 1-已接单 2-待取件 3-配送中 4-待确认 5-已完成 6-已取消") @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getUserId();
        IPage<TaskVO> page = taskService.getMyTasks(userId, role, status, current, size);
        PageResult<TaskVO> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return Result.success(result);
    }
}
