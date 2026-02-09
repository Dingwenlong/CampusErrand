package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.Transaction;

import java.math.BigDecimal;

public interface TransactionService extends IService<Transaction> {

    IPage<Transaction> getTransactionList(Long userId, Integer direction, Long current, Long size);

    String generateTransactionNo();

    /**
     * 创建交易记录
     * @param userId 用户ID
     * @param transactionType 交易类型：1-充值 2-提现 3-任务支付 4-任务收入 5-退款
     * @param direction 收支方向：1-收入 2-支出
     * @param amount 交易金额
     * @param balance 交易后余额
     * @param relatedId 关联业务ID
     * @param relatedType 关联业务类型
     * @param remark 备注
     * @return 交易记录
     */
    Transaction createTransaction(Long userId, Integer transactionType, Integer direction,
                                  BigDecimal amount, BigDecimal balance, Long relatedId,
                                  String relatedType, String remark);

    /**
     * 虚拟充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 是否成功
     */
    boolean recharge(Long userId, BigDecimal amount);

    /**
     * 虚拟提现
     * @param userId 用户ID
     * @param amount 提现金额
     * @return 是否成功
     */
    boolean withdraw(Long userId, BigDecimal amount);

    /**
     * 任务支付（发布任务时支付赏金）
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param amount 支付金额
     * @return 是否成功
     */
    boolean taskPayment(Long userId, Long taskId, BigDecimal amount);

    /**
     * 任务收入（任务完成后给接单者）
     * @param userId 用户ID（接单者）
     * @param taskId 任务ID
     * @param amount 收入金额
     * @return 是否成功
     */
    boolean taskIncome(Long userId, Long taskId, BigDecimal amount);

    /**
     * 任务退款（任务取消时退款）
     * @param userId 用户ID（发布者）
     * @param taskId 任务ID
     * @param amount 退款金额
     * @return 是否成功
     */
    boolean taskRefund(Long userId, Long taskId, BigDecimal amount);
}
