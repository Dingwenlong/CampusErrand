package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.common.PageResult;
import com.campus.errand.common.Result;
import com.campus.errand.dto.RechargeDTO;
import com.campus.errand.dto.SetPayPasswordDTO;
import com.campus.errand.dto.VerifyPayPasswordDTO;
import com.campus.errand.dto.WithdrawDTO;
import com.campus.errand.entity.Transaction;
import com.campus.errand.entity.UserWallet;
import com.campus.errand.service.TransactionService;
import com.campus.errand.service.UserWalletService;
import com.campus.errand.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "钱包管理", description = "钱包相关接口")
@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final UserWalletService userWalletService;
    private final TransactionService transactionService;
    
    @Autowired
    public WalletController(UserWalletService userWalletService, TransactionService transactionService) {
        this.userWalletService = userWalletService;
        this.transactionService = transactionService;
    }

    @Operation(summary = "获取钱包信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getWalletInfo() {
        Long userId = UserContext.getUserId();
        UserWallet wallet = userWalletService.getByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("balance", wallet.getBalance());
        result.put("frozenAmount", wallet.getFrozenAmount());
        result.put("totalIncome", wallet.getTotalIncome());
        result.put("totalExpense", wallet.getTotalExpense());
        result.put("hasPayPassword", wallet.getPayPassword() != null && !wallet.getPayPassword().isEmpty() ? 1 : 0);

        return Result.success(result);
    }

    @Operation(summary = "设置支付密码")
    @PostMapping("/pay-password")
    public Result<Boolean> setPayPassword(@Valid @RequestBody SetPayPasswordDTO setPayPasswordDTO) {
        Long userId = UserContext.getUserId();
        boolean success = userWalletService.setPayPassword(userId, setPayPasswordDTO.getPayPassword());
        if (!success) {
            return Result.error("设置支付密码失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "验证支付密码")
    @PostMapping("/verify-pay-password")
    public Result<Boolean> verifyPayPassword(@Valid @RequestBody VerifyPayPasswordDTO verifyPayPasswordDTO) {
        Long userId = UserContext.getUserId();
        boolean valid = userWalletService.verifyPayPassword(userId, verifyPayPasswordDTO.getPayPassword());
        if (!valid) {
            return Result.error("支付密码错误");
        }
        return Result.success(true);
    }

    @Operation(summary = "虚拟充值")
    @PostMapping("/recharge")
    public Result<Boolean> recharge(@Valid @RequestBody RechargeDTO rechargeDTO) {
        Long userId = UserContext.getUserId();

        // 检查是否设置了支付密码
        UserWallet wallet = userWalletService.getByUserId(userId);
        if (wallet.getPayPassword() == null || wallet.getPayPassword().isEmpty()) {
            return Result.error("请先设置支付密码");
        }

        // 验证支付密码
        if (!userWalletService.verifyPayPassword(userId, rechargeDTO.getPayPassword())) {
            return Result.error("支付密码错误");
        }

        boolean success = transactionService.recharge(userId, rechargeDTO.getAmount());
        if (!success) {
            return Result.error("充值失败");
        }

        return Result.success(true);
    }

    @Operation(summary = "虚拟提现")
    @PostMapping("/withdraw")
    public Result<Boolean> withdraw(@Valid @RequestBody WithdrawDTO withdrawDTO) {
        Long userId = UserContext.getUserId();

        // 验证支付密码
        boolean valid = userWalletService.verifyPayPassword(userId, withdrawDTO.getPayPassword());
        if (!valid) {
            return Result.error("支付密码错误");
        }

        // 检查余额是否充足
        UserWallet wallet = userWalletService.getByUserId(userId);
        if (wallet.getBalance().compareTo(withdrawDTO.getAmount()) < 0) {
            return Result.error("余额不足");
        }

        boolean success = transactionService.withdraw(userId, withdrawDTO.getAmount());
        if (!success) {
            return Result.error("提现失败");
        }

        return Result.success(true);
    }

    @Operation(summary = "获取交易流水")
    @GetMapping("/transactions")
    public Result<PageResult<Transaction>> getTransactions(
            @Parameter(description = "收支方向：0-全部 1-收入 2-支出") @RequestParam(required = false, defaultValue = "0") Integer direction,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getUserId();

        Integer dir = direction == 0 ? null : direction;
        IPage<Transaction> page = transactionService.getTransactionList(userId, dir, current, size);

        PageResult<Transaction> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return Result.success(result);
    }

    @Operation(summary = "获取交易类型名称")
    @GetMapping("/transaction-types")
    public Result<Map<Integer, String>> getTransactionTypes() {
        Map<Integer, String> types = new HashMap<>();
        types.put(1, "充值");
        types.put(2, "提现");
        types.put(3, "任务支付");
        types.put(4, "任务收入");
        types.put(5, "退款");
        return Result.success(types);
    }
}
