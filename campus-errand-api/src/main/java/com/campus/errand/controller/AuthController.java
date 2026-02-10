package com.campus.errand.controller;

import com.campus.errand.common.Result;
import com.campus.errand.dto.LoginDTO;
import com.campus.errand.entity.User;
import com.campus.errand.entity.UserWallet;
import com.campus.errand.service.UserService;
import com.campus.errand.service.UserWalletService;
import com.campus.errand.util.JwtUtil;
import com.campus.errand.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "认证管理", description = "登录认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final UserWalletService userWalletService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, UserWalletService userWalletService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userWalletService = userWalletService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "微信小程序登录")
    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@Valid @RequestBody LoginDTO loginDTO) {
        logger.info("========== 收到微信登录请求 ==========");
        logger.info("请求参数: code={}, nickname={}, avatar={}, gender={}",
                loginDTO.getCode(),
                loginDTO.getNickname(),
                loginDTO.getAvatar(),
                loginDTO.getGender());

        // 模拟获取openid（实际应调用微信接口）
        String openid = "mock_openid_" + loginDTO.getCode();
        logger.info("生成的openid: {}", openid);

        // 查询用户是否存在
        User user = userService.getByOpenid(openid);
        boolean isNewUser = false;

        if (user == null) {
            // 新用户注册
            user = new User();
            user.setOpenid(openid);
            user.setNickname(loginDTO.getNickname());
            user.setAvatar(loginDTO.getAvatar());
            user.setGender(loginDTO.getGender());
            user.setStatus(1);
            user.setCreditScore(100);
            userService.save(user);

            // 创建用户钱包
            UserWallet wallet = new UserWallet();
            wallet.setUserId(user.getId());
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            wallet.setTotalIncome(BigDecimal.ZERO);
            wallet.setTotalExpense(BigDecimal.ZERO);
            userWalletService.save(wallet);

            isNewUser = true;
        } else {
            // 更新用户信息
            if (loginDTO.getNickname() != null) {
                user.setNickname(loginDTO.getNickname());
            }
            if (loginDTO.getAvatar() != null) {
                user.setAvatar(loginDTO.getAvatar());
            }
            if (loginDTO.getGender() != null) {
                user.setGender(loginDTO.getGender());
            }
            userService.updateById(user);
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), openid);

        // 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setIsNewUser(isNewUser ? 1 : 0);

        logger.info("登录成功, 用户ID: {}, 是否新用户: {}", user.getId(), isNewUser);
        logger.info("========== 微信登录请求处理完成 ==========");

        return Result.success(loginVO);
    }
}
