package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.UserWallet;

import java.math.BigDecimal;

public interface UserWalletService extends IService<UserWallet> {

    UserWallet getByUserId(Long userId);

    boolean setPayPassword(Long userId, String payPassword);

    boolean verifyPayPassword(Long userId, String payPassword);

    boolean hasPayPassword(Long userId);

    /**
     * 冻结金额
     * @param userId 用户ID
     * @param amount 冻结金额
     * @return 是否成功
     */
    boolean freezeAmount(Long userId, BigDecimal amount);

    /**
     * 解冻金额
     * @param userId 用户ID
     * @param amount 解冻金额
     * @return 是否成功
     */
    boolean unfreezeAmount(Long userId, BigDecimal amount);

    /**
     * 扣除余额（用于任务支付）
     * @param userId 用户ID
     * @param amount 扣除金额
     * @return 是否成功
     */
    boolean deductBalance(Long userId, BigDecimal amount);

    /**
     * 增加余额（用于任务收入）
     * @param userId 用户ID
     * @param amount 增加金额
     * @return 是否成功
     */
    boolean addBalance(Long userId, BigDecimal amount);

    /**
     * 检查余额是否充足
     * @param userId 用户ID
     * @param amount 需要金额
     * @return 是否充足
     */
    boolean hasEnoughBalance(Long userId, BigDecimal amount);
}
