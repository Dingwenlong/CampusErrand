package com.campus.errand.controller;

import com.campus.errand.common.Result;
import com.campus.errand.dto.AdminChangePasswordDTO;
import com.campus.errand.dto.AdminLoginDTO;
import com.campus.errand.entity.AdminAccount;
import com.campus.errand.service.AdminAccountService;
import com.campus.errand.service.AdminService;
import com.campus.errand.util.JwtUtil;
import com.campus.errand.util.UserContext;
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
    private final AdminAccountService adminAccountService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AdminController(AdminService adminService, AdminAccountService adminAccountService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.adminAccountService = adminAccountService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        AdminAccount account = adminAccountService.authenticate(loginDTO.getUsername(), loginDTO.getPassword());
        if (account == null) {
            return Result.error("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(account.getId(), account.getUsername(), "admin");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", account.getUsername());
        result.put("nickname", account.getNickname());
        result.put("role", account.getRole());
        result.put("forcePasswordChange", account.getForcePasswordChange());

        return Result.success(result);
    }

    @Operation(summary = "获取管理员信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getAdminInfo() {
        AdminAccount account = adminAccountService.getById(UserContext.getUserId());
        if (account == null) {
            return Result.error("管理员不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("username", account.getUsername());
        result.put("role", account.getRole());
        result.put("nickname", account.getNickname());
        result.put("forcePasswordChange", account.getForcePasswordChange());
        return Result.success(result);
    }

    @Operation(summary = "修改管理员密码")
    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@Valid @RequestBody AdminChangePasswordDTO dto) {
        boolean success = adminAccountService.changePassword(UserContext.getUserId(), dto.getOldPassword(), dto.getNewPassword());
        if (!success) {
            return Result.error("原密码错误或管理员不存在");
        }
        return Result.success(true);
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
