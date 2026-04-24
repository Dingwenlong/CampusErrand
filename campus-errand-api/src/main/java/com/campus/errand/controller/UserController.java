package com.campus.errand.controller;

import com.campus.errand.common.Result;
import com.campus.errand.dto.UserVerifyDTO;
import com.campus.errand.dto.UserProfileDTO;
import com.campus.errand.entity.User;
import com.campus.errand.entity.UserWallet;
import com.campus.errand.service.FileStorageService;
import com.campus.errand.service.UserService;
import com.campus.errand.service.UserWalletService;
import com.campus.errand.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserWalletService userWalletService;
    private final FileStorageService fileStorageService;

    @Autowired
    public UserController(UserService userService, UserWalletService userWalletService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.userWalletService = userWalletService;
        this.fileStorageService = fileStorageService;
    }

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

    @Operation(summary = "更新当前用户资料")
    @PutMapping("/profile")
    public Result<Boolean> updateProfile(@RequestBody UserProfileDTO profileDTO) {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (profileDTO.getNickname() != null) {
            user.setNickname(profileDTO.getNickname());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }
        if (profileDTO.getGender() != null) {
            user.setGender(profileDTO.getGender());
        }
        if (profileDTO.getSchoolName() != null) {
            user.setSchoolName(profileDTO.getSchoolName());
        }

        return Result.success(userService.updateById(user));
    }

    @Operation(summary = "上传头像")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadAvatar(@RequestPart("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        String url = fileStorageService.storeImage(file, "avatar", userId);
        User user = userService.getById(userId);
        if (user != null) {
            user.setAvatar(url);
            userService.updateById(user);
        }
        return Result.success(Map.of("url", url));
    }

    @Operation(summary = "上传身份证照片")
    @PostMapping(value = "/upload-idcard", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadIdCard(@RequestPart("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        String url = fileStorageService.storeImage(file, "idcard", userId);
        return Result.success(Map.of("url", url));
    }

    @Operation(summary = "获取实名认证状态")
    @GetMapping("/verify-status")
    public Result<Map<String, Object>> getVerifyStatus() {
        User user = userService.getById(UserContext.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        Integer status = user.getVerifyStatus();
        if (status == null) {
            status = user.getIsVerified() != null && user.getIsVerified() == 1 ? 2 : 0;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("status", toVerifyStatusName(status));
        data.put("statusCode", status);
        data.put("realName", user.getRealName());
        data.put("idCard", user.getIdCard());
        data.put("reason", user.getVerifyRejectReason());
        if (user.getVerifyTime() != null) {
            data.put("verifyTime", user.getVerifyTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return Result.success(data);
    }

    @Operation(summary = "提交实名认证")
    @PostMapping("/verify")
    public Result<Boolean> submitVerify(@Valid @RequestBody UserVerifyDTO dto) {
        User user = userService.getById(UserContext.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getVerifyStatus() != null && user.getVerifyStatus() == 2) {
            return Result.error("实名认证已通过，不能重复提交");
        }
        user.setRealName(dto.getRealName());
        user.setIdCard(dto.getIdCard().toUpperCase());
        user.setIdCardFrontImage(dto.getFrontImage());
        user.setIdCardBackImage(dto.getBackImage());
        user.setVerifyStatus(1);
        user.setIsVerified(0);
        user.setVerifyRejectReason(null);
        user.setVerifySubmitTime(LocalDateTime.now());
        user.setVerifyTime(null);
        return Result.success(userService.updateById(user));
    }

    private String toVerifyStatusName(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "pending";
            case 2 -> "verified";
            case 3 -> "rejected";
            default -> "unverified";
        };
    }
}
