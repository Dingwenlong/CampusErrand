package com.campus.errand.controller;

import com.campus.errand.common.Result;
import com.campus.errand.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "管理员管理", description = "后台管理相关接口")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        String token = adminService.login(username, password);
        if (token == null) {
            return Result.error("用户名或密码错误");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", username);
        return Result.success(data);
    }

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        return Result.success(adminService.getDashboardData());
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/user/list")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getUserList(current, size, keyword));
    }

    @Operation(summary = "修改用户状态")
    @PostMapping("/user/{id}/status")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        boolean success = adminService.updateUserStatus(id, status);
        return Result.success(success);
    }
}
