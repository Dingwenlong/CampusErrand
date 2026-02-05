package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.common.PageResult;
import com.campus.errand.common.Result;
import com.campus.errand.dto.TaskPublishDTO;
import com.campus.errand.service.TaskService;
import com.campus.errand.util.UserContext;
import com.campus.errand.vo.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "任务管理", description = "跑腿任务相关接口")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

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
}
