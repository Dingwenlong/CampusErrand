package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.common.PageResult;
import com.campus.errand.common.Result;
import com.campus.errand.service.AdminWalletService;
import com.campus.errand.vo.TransactionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "后台-钱包管理", description = "后台钱包管理接口")
@RestController
@RequestMapping("/admin/wallet")
public class AdminWalletController {

    private final AdminWalletService adminWalletService;

    @Autowired
    public AdminWalletController(AdminWalletService adminWalletService) {
        this.adminWalletService = adminWalletService;
    }

    @Operation(summary = "获取交易流水")
    @GetMapping("/transactions")
    public Result<PageResult<TransactionVO>> getTransactions(
            @Parameter(description = "交易类型：1-充值 2-提现 3-任务支付 4-任务收入 5-退款") @RequestParam(required = false) Integer transactionType,
            @Parameter(description = "交易方向：1-收入 2-支出") @RequestParam(required = false) Integer direction,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<TransactionVO> page = adminWalletService.getTransactions(
                transactionType, direction, userId, startTime, endTime, current, size);
        PageResult<TransactionVO> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return Result.success(result);
    }

    @Operation(summary = "获取资金统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> data = adminWalletService.getWalletStats();
        return Result.success(data);
    }
}
