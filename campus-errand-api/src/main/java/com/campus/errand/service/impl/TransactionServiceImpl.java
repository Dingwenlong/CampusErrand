package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.entity.Transaction;
import com.campus.errand.entity.UserWallet;
import com.campus.errand.mapper.TransactionMapper;
import com.campus.errand.service.TransactionService;
import com.campus.errand.service.UserWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, Transaction> implements TransactionService {

    private final UserWalletService userWalletService;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction createTransaction(Long userId, Integer transactionType, Integer direction,
                                         BigDecimal amount, BigDecimal balance, Long relatedId,
                                         String relatedType, String remark) {
        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setUserId(userId);
        transaction.setTransactionType(transactionType);
        transaction.setDirection(direction);
        transaction.setAmount(amount);
        transaction.setBalance(balance);
        transaction.setRelatedId(relatedId);
        transaction.setRelatedType(relatedType);
        transaction.setRemark(remark);
        transaction.setStatus(1); // 成功

        save(transaction);
        return transaction;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recharge(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 获取用户钱包
        UserWallet wallet = userWalletService.getByUserId(userId);
        if (wallet == null) {
            return false;
        }

        // 增加余额
        boolean success = userWalletService.addBalance(userId, amount);
        if (!success) {
            return false;
        }

        // 获取更新后的余额
        wallet = userWalletService.getByUserId(userId);

        // 创建交易记录
        createTransaction(userId, 1, 1, amount, wallet.getBalance(),
                null, "RECHARGE", "虚拟充值");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdraw(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 获取用户钱包
        UserWallet wallet = userWalletService.getByUserId(userId);
        if (wallet == null) {
            return false;
        }

        // 检查余额是否充足
        if (wallet.getBalance().compareTo(amount) < 0) {
            return false;
        }

        // 扣除余额
        boolean success = userWalletService.deductBalance(userId, amount);
        if (!success) {
            return false;
        }

        // 获取更新后的余额
        wallet = userWalletService.getByUserId(userId);

        // 创建交易记录
        createTransaction(userId, 2, 2, amount, wallet.getBalance(),
                null, "WITHDRAW", "虚拟提现");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean taskPayment(Long userId, Long taskId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 获取用户钱包
        UserWallet wallet = userWalletService.getByUserId(userId);
        if (wallet == null) {
            return false;
        }

        // 检查余额是否充足（考虑冻结金额）
        BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getFrozenAmount());
        if (availableBalance.compareTo(amount) < 0) {
            return false;
        }

        // 先冻结金额
        boolean freezeSuccess = userWalletService.freezeAmount(userId, amount);
        if (!freezeSuccess) {
            return false;
        }

        // 然后扣除余额（从冻结中扣除）
        boolean deductSuccess = userWalletService.deductBalance(userId, amount);
        if (!deductSuccess) {
            // 回滚冻结
            userWalletService.unfreezeAmount(userId, amount);
            return false;
        }

        // 解冻（因为已经扣除了）
        userWalletService.unfreezeAmount(userId, amount);

        // 获取更新后的余额
        wallet = userWalletService.getByUserId(userId);

        // 创建交易记录
        createTransaction(userId, 3, 2, amount, wallet.getBalance(),
                taskId, "TASK", "任务支付-任务ID:" + taskId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean taskIncome(Long userId, Long taskId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 获取用户钱包
        UserWallet wallet = userWalletService.getByUserId(userId);
        if (wallet == null) {
            return false;
        }

        // 增加余额
        boolean success = userWalletService.addBalance(userId, amount);
        if (!success) {
            return false;
        }

        // 获取更新后的余额
        wallet = userWalletService.getByUserId(userId);

        // 创建交易记录
        createTransaction(userId, 4, 1, amount, wallet.getBalance(),
                taskId, "TASK", "任务收入-任务ID:" + taskId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean taskRefund(Long userId, Long taskId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 获取用户钱包
        UserWallet wallet = userWalletService.getByUserId(userId);
        if (wallet == null) {
            return false;
        }

        // 增加余额（退款）
        boolean success = userWalletService.addBalance(userId, amount);
        if (!success) {
            return false;
        }

        // 获取更新后的余额
        wallet = userWalletService.getByUserId(userId);

        // 创建交易记录
        createTransaction(userId, 5, 1, amount, wallet.getBalance(),
                taskId, "TASK", "任务退款-任务ID:" + taskId);

        return true;
    }
}
