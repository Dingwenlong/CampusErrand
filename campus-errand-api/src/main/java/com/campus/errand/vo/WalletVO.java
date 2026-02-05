package com.campus.errand.vo;

import java.math.BigDecimal;

public class WalletVO {

    private Long userId;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private BigDecimal availableBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private Integer hasPayPassword;

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

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
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

    public Integer getHasPayPassword() {
        return hasPayPassword;
    }

    public void setHasPayPassword(Integer hasPayPassword) {
        this.hasPayPassword = hasPayPassword;
    }
}
