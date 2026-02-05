package com.campus.errand.controller;

import com.campus.errand.common.Result;
import com.campus.errand.entity.User;
import com.campus.errand.entity.UserWallet;
import com.campus.errand.service.UserService;
import com.campus.errand.service.UserWalletService;
import com.campus.errand.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserWalletService userWalletService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getCurrentUser() {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        UserWallet wallet = userWalletService.getByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("wallet", wallet);
        
        return Result.success(result);
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }
}
