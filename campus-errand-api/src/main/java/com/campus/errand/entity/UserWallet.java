package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("tb_user_wallet")
public class UserWallet extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private String payPassword;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }
}
