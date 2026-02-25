package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.vo.TransactionVO;

import java.math.BigDecimal;
import java.util.Map;

public interface AdminWalletService {

    /**
     * 获取交易流水
     */
    IPage<TransactionVO> getTransactions(Integer transactionType, Integer direction, Long userId,
                                         String startTime, String endTime, Long current, Long size);

    /**
     * 获取资金统计
     */
    Map<String, Object> getWalletStats();

    /**
     * 管理员充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @param remark 备注
     * @return 是否成功
     */
    boolean adminRecharge(Long userId, BigDecimal amount, String remark);
}
