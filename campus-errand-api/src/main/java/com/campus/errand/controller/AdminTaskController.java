package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.common.PageResult;
import com.campus.errand.common.Result;
import com.campus.errand.service.AdminTaskService;
import com.campus.errand.vo.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "后台-任务管理", description = "后台任务管理接口")
@RestController
@RequestMapping("/admin/task")
public class AdminTaskController {

    private final AdminTaskService adminTaskService;

    @Autowired
    public AdminTaskController(AdminTaskService adminTaskService) {
        this.adminTaskService = adminTaskService;
    }

    @Operation(summary = "获取任务列表")
    @GetMapping("/list")
    public Result<PageResult<TaskVO>> list(
            @Parameter(description = "任务类型：1-取快递 2-代买 3-送件 4-其他") @RequestParam(required = false) Integer taskType,
            @Parameter(description = "任务状态：0-待接单 1-已接单 2-待取件 3-配送中 4-待确认 5-已完成 6-已取消") @RequestParam(required = false) Integer status,
            @Parameter(description = "关键词（标题/发布者）") @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<TaskVO> page = adminTaskService.getTaskList(taskType, status, keyword, current, size);
        PageResult<TaskVO> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return Result.success(result);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<TaskVO> detail(@PathVariable Long id) {
        TaskVO taskVO = adminTaskService.getTaskDetail(id);
        if (taskVO == null) {
            return Result.error("任务不存在");
        }
        return Result.success(taskVO);
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancel(
            @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam String reason) {
        boolean success = adminTaskService.cancelTask(id, reason);
        if (!success) {
            return Result.error("取消任务失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = adminTaskService.deleteTask(id);
        if (!success) {
            return Result.error("删除任务失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "获取任务统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = adminTaskService.getTaskStats();
        return Result.success(data);
    }
}
