package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.entity.Transaction;
import com.campus.errand.mapper.TransactionMapper;
import com.campus.errand.service.TransactionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, Transaction> implements TransactionService {

    @Override
    public IPage<Transaction> getTransactionList(Long userId, Integer direction, Long current, Long size) {
        Page<Transaction> page = new Page<>(current, size);
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId);

        if (direction != null && direction != 0) {
            wrapper.eq(Transaction::getDirection, direction);
        }

        wrapper.orderByDesc(Transaction::getCreateTime);

        return page(page, wrapper);
    }

    @Override
    public String generateTransactionNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%06d", new Random().nextInt(1000000));
        return "TX" + dateStr + randomStr;
    }
}
