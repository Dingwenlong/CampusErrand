package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.vo.TransactionVO;

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
}
