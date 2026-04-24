package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.common.Result;
import com.campus.errand.dto.AdminVerifyRejectDTO;
import com.campus.errand.entity.User;
import com.campus.errand.entity.UserWallet;
import com.campus.errand.service.UserService;
import com.campus.errand.service.UserWalletService;
import com.campus.errand.util.UserContext;
import com.campus.errand.vo.UserDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "后台用户管理", description = "后台用户管理相关接口")
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    private final UserService userService;
    private final UserWalletService userWalletService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public AdminUserController(UserService userService, UserWalletService userWalletService) {
        this.userService = userService;
        this.userWalletService = userWalletService;
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
    public Result<UserDetailVO> getUserDetail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        UserDetailVO vo = new UserDetailVO();
        vo.setId(user.getId());
        vo.setOpenid(user.getOpenid());
        vo.setUnionid(user.getUnionid());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setGender(user.getGender());
        vo.setUserType(user.getUserType());
        vo.setStatus(user.getStatus());
        vo.setIsVerified(user.getIsVerified());
        vo.setRealName(user.getRealName());
        vo.setIdCard(user.getIdCard());
        vo.setStudentId(user.getStudentId());
        vo.setSchoolName(user.getSchoolName());
        vo.setCreditScore(user.getCreditScore());
        vo.setTotalOrders(user.getTotalOrders());
        vo.setCompletedOrders(user.getCompletedOrders());
        vo.setCancelledOrders(user.getCancelledOrders());

        if (user.getCreateTime() != null) {
            vo.setCreateTime(user.getCreateTime().format(DATE_FORMATTER));
        }
        if (user.getUpdateTime() != null) {
            vo.setUpdateTime(user.getUpdateTime().format(DATE_FORMATTER));
        }

        UserWallet wallet = userWalletService.getByUserId(id);
        if (wallet != null) {
            vo.setBalance(wallet.getBalance());
            vo.setFrozenAmount(wallet.getFrozenAmount());
            vo.setTotalIncome(wallet.getTotalIncome());
            vo.setTotalExpense(wallet.getTotalExpense());
        }

        return Result.success(vo);
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

    @Operation(summary = "获取实名认证审核列表")
    @GetMapping("/verify/list")
    public Result<Page<User>> getVerifyList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(User::getVerifyStatus, status);
        } else {
            wrapper.in(User::getVerifyStatus, 1, 2, 3);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword.trim())
                    .or()
                    .like(User::getRealName, keyword.trim())
                    .or()
                    .like(User::getPhone, keyword.trim()));
        }
        wrapper.orderByDesc(User::getVerifySubmitTime);
        return Result.success(userService.page(page, wrapper));
    }

    @Operation(summary = "通过实名认证")
    @PostMapping("/verify/{id}/approve")
    public Result<Boolean> approveVerify(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setVerifyStatus(2);
        user.setIsVerified(1);
        user.setVerifyRejectReason(null);
        user.setVerifyTime(LocalDateTime.now());
        user.setVerifyAdminId(UserContext.getUserId());
        return Result.success(userService.updateById(user));
    }

    @Operation(summary = "驳回实名认证")
    @PostMapping("/verify/{id}/reject")
    public Result<Boolean> rejectVerify(@PathVariable Long id, @Valid @RequestBody AdminVerifyRejectDTO dto) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setVerifyStatus(3);
        user.setIsVerified(0);
        user.setVerifyRejectReason(dto.getReason());
        user.setVerifyTime(LocalDateTime.now());
        user.setVerifyAdminId(UserContext.getUserId());
        return Result.success(userService.updateById(user));
    }
}
