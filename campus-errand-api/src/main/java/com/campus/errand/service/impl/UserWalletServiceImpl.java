package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.entity.UserWallet;
import com.campus.errand.mapper.UserWalletMapper;
import com.campus.errand.service.UserWalletService;
import com.campus.errand.util.MD5Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserWalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWallet> implements UserWalletService {

    @Override
    public UserWallet getByUserId(Long userId) {
        LambdaQueryWrapper<UserWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWallet::getUserId, userId);
        return getOne(wrapper);
    }

    @Override
    public boolean setPayPassword(Long userId, String payPassword) {
        UserWallet wallet = getByUserId(userId);
        if (wallet == null) {
            return false;
        }
        
        String encryptedPassword = MD5Util.encrypt(payPassword);
        wallet.setPayPassword(encryptedPassword);
        return updateById(wallet);
    }

    @Override
    public boolean verifyPayPassword(Long userId, String payPassword) {
        UserWallet wallet = getByUserId(userId);
        if (wallet == null || wallet.getPayPassword() == null) {
            return false;
        }
        
        return MD5Util.verify(payPassword, wallet.getPayPassword());
    }

    @Override
    public boolean hasPayPassword(Long userId) {
        UserWallet wallet = getByUserId(userId);
        return wallet != null && wallet.getPayPassword() != null && !wallet.getPayPassword().isEmpty();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeAmount(Long userId, BigDecimal amount) {
        UserWallet wallet = getByUserId(userId);
        if (wallet == null) {
            return false;
        }

        // 检查可用余额是否充足
        BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getFrozenAmount());
        if (availableBalance.compareTo(amount) < 0) {
            return false;
        }

        // 更新冻结金额
        LambdaUpdateWrapper<UserWallet> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserWallet::getId, wallet.getId())
                .set(UserWallet::getFrozenAmount, wallet.getFrozenAmount().add(amount));

        return update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeAmount(Long userId, BigDecimal amount) {
        UserWallet wallet = getByUserId(userId);
        if (wallet == null) {
            return false;
        }

        // 检查冻结金额是否足够
        if (wallet.getFrozenAmount().compareTo(amount) < 0) {
            return false;
        }

        // 更新冻结金额
        LambdaUpdateWrapper<UserWallet> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserWallet::getId, wallet.getId())
                .set(UserWallet::getFrozenAmount, wallet.getFrozenAmount().subtract(amount));

        return update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductBalance(Long userId, BigDecimal amount) {
        UserWallet wallet = getByUserId(userId);
        if (wallet == null) {
            return false;
        }

        // 检查余额是否充足
        if (wallet.getBalance().compareTo(amount) < 0) {
            return false;
        }

        // 更新余额和累计支出
        LambdaUpdateWrapper<UserWallet> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserWallet::getId, wallet.getId())
                .set(UserWallet::getBalance, wallet.getBalance().subtract(amount))
                .set(UserWallet::getTotalExpense, wallet.getTotalExpense().add(amount));

        return update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBalance(Long userId, BigDecimal amount) {
        UserWallet wallet = getByUserId(userId);
        if (wallet == null) {
            return false;
        }

        // 更新余额和累计收入
        LambdaUpdateWrapper<UserWallet> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserWallet::getId, wallet.getId())
                .set(UserWallet::getBalance, wallet.getBalance().add(amount))
                .set(UserWallet::getTotalIncome, wallet.getTotalIncome().add(amount));

        return update(wrapper);
    }

    @Override
    public boolean hasEnoughBalance(Long userId, BigDecimal amount) {
        UserWallet wallet = getByUserId(userId);
        if (wallet == null) {
            return false;
        }
        BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getFrozenAmount());
        return availableBalance.compareTo(amount) >= 0;
    }
}
