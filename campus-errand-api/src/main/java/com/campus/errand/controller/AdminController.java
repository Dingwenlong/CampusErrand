package com.campus.errand.controller;

import com.campus.errand.common.Result;
import com.campus.errand.dto.AdminLoginDTO;
import com.campus.errand.service.AdminService;
import com.campus.errand.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "后台管理", description = "后台管理相关接口")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    // 默认管理员账号密码（实际应该存储在数据库中并加密）
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    @Autowired
    public AdminController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        // 验证用户名密码
        if (!DEFAULT_ADMIN_USERNAME.equals(loginDTO.getUsername()) ||
            !DEFAULT_ADMIN_PASSWORD.equals(loginDTO.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(0L, "admin");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", DEFAULT_ADMIN_USERNAME);
        result.put("role", "admin");

        return Result.success(result);
    }

    @Operation(summary = "获取管理员信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getAdminInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("username", DEFAULT_ADMIN_USERNAME);
        result.put("role", "admin");
        result.put("nickname", "管理员");
        return Result.success(result);
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
