package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.common.PageResult;
import com.campus.errand.common.Result;
import com.campus.errand.dto.PayPasswordDTO;
import com.campus.errand.entity.Transaction;
import com.campus.errand.entity.UserWallet;
import com.campus.errand.service.TransactionService;
import com.campus.errand.service.UserWalletService;
import com.campus.errand.util.UserContext;
import com.campus.errand.vo.TransactionVO;
import com.campus.errand.vo.WalletVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "钱包管理", description = "用户钱包相关接口")
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

    @Operation(summary = "获取钱包余额")
    @GetMapping("/balance")
    public Result<WalletVO> getBalance() {
        Long userId = UserContext.getUserId();
        UserWallet wallet = userWalletService.getByUserId(userId);

        if (wallet == null) {
            return Result.error("钱包不存在");
        }

        WalletVO vo = new WalletVO();
        BeanUtils.copyProperties(wallet, vo);

        // 计算可用余额
        vo.setAvailableBalance(wallet.getBalance().subtract(wallet.getFrozenAmount()));

        // 判断是否设置了支付密码
        vo.setHasPayPassword(wallet.getPayPassword() != null && !wallet.getPayPassword().isEmpty() ? 1 : 0);

        return Result.success(vo);
    }

    @Operation(summary = "获取交易流水")
    @GetMapping("/transactions")
    public Result<PageResult<TransactionVO>> getTransactions(
            @Parameter(description = "收支方向：0-全部 1-收入 2-支出") @RequestParam(defaultValue = "0") Integer direction,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getUserId();

        IPage<Transaction> page = transactionService.getTransactionList(userId, direction, current, size);

        List<TransactionVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<TransactionVO> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), voList);
        return Result.success(result);
    }

    @Operation(summary = "设置支付密码")
    @PostMapping("/pay-password")
    public Result<Boolean> setPayPassword(@Valid @RequestBody PayPasswordDTO payPasswordDTO) {
        Long userId = UserContext.getUserId();

        // 检查两次密码是否一致
        if (!payPasswordDTO.getPayPassword().equals(payPasswordDTO.getConfirmPassword())) {
            return Result.error("两次输入的密码不一致");
        }

        // 检查是否已设置过支付密码
        if (userWalletService.hasPayPassword(userId)) {
            return Result.error("支付密码已设置，如需修改请使用修改接口");
        }

        boolean success = userWalletService.setPayPassword(userId, payPasswordDTO.getPayPassword());
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("设置支付密码失败");
        }
    }

    @Operation(summary = "验证支付密码")
    @PostMapping("/verify-pay-password")
    public Result<Boolean> verifyPayPassword(@RequestParam String payPassword) {
        Long userId = UserContext.getUserId();

        boolean valid = userWalletService.verifyPayPassword(userId, payPassword);
        if (valid) {
            return Result.success(true);
        } else {
            return Result.error("支付密码错误");
        }
    }

    @Operation(summary = "检查是否已设置支付密码")
    @GetMapping("/has-pay-password")
    public Result<Boolean> hasPayPassword() {
        Long userId = UserContext.getUserId();
        boolean hasPassword = userWalletService.hasPayPassword(userId);
        return Result.success(hasPassword);
    }

    private TransactionVO convertToVO(Transaction transaction) {
        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(transaction, vo);

        // 设置交易类型名称
        vo.setTransactionTypeName(getTransactionTypeName(transaction.getTransactionType()));

        // 设置收支方向名称
        vo.setDirectionName(transaction.getDirection() == 1 ? "收入" : "支出");

        // 设置状态名称
        vo.setStatusName(getStatusName(transaction.getStatus()));

        return vo;
    }

    private String getTransactionTypeName(Integer type) {
        switch (type) {
            case 1: return "充值";
            case 2: return "提现";
            case 3: return "任务支付";
            case 4: return "任务收入";
            case 5: return "退款";
            default: return "其他";
        }
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "处理中";
            case 1: return "成功";
            case 2: return "失败";
            default: return "未知";
        }
    }
}
