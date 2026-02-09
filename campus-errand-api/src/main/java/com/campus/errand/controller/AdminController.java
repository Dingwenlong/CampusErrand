package com.campus.errand.controller;

import com.campus.errand.common.Result;
import com.campus.errand.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "后台管理", description = "后台管理相关接口")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = adminService.getDashboardData();
        return Result.success(data);
    }

    @Operation(summary = "获取任务状态分布")
    @GetMapping("/task-status-stats")
    public Result<Map<String, Object>> getTaskStatusStats() {
        Map<String, Object> data = adminService.getTaskStatusStats();
        return Result.success(data);
    }

    @Operation(summary = "获取交易趋势数据")
    @GetMapping("/amount-trend")
    public Result<Map<String, Object>> getAmountTrend() {
        Map<String, Object> data = adminService.getAmountTrend();
        return Result.success(data);
    }

    @Operation(summary = "获取用户增长趋势")
    @GetMapping("/user-growth")
    public Result<Map<String, Object>> getUserGrowth() {
        Map<String, Object> data = adminService.getUserGrowth();
        return Result.success(data);
    }
}
