package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.common.Result;
import com.campus.errand.entity.User;
import com.campus.errand.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台用户管理", description = "后台用户管理相关接口")
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    private final UserService userService;

    @Autowired
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 根据关键词搜索（昵称或手机号）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(User::getNickname, keyword)
                   .or()
                   .like(User::getPhone, keyword);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> result = userService.page(page, wrapper);
        return Result.success(result);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @Operation(summary = "更新用户状态")
    @PostMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "更新用户信用分")
    @PostMapping("/{id}/credit")
    public Result<Void> updateUserCredit(
            @PathVariable Long id,
            @RequestParam Integer creditScore) {
        
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setCreditScore(creditScore);
        userService.updateById(user);
        return Result.success();
    }
}
