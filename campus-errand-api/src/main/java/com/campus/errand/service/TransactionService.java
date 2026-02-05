package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.Transaction;

public interface TransactionService extends IService<Transaction> {

    IPage<Transaction> getTransactionList(Long userId, Integer direction, Long current, Long size);

    String generateTransactionNo();
}
