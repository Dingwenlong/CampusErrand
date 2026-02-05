package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("tb_transaction")
public class Transaction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String transactionNo;
    private Long userId;
    private Integer transactionType;
    private Integer direction;
    private BigDecimal amount;
    private BigDecimal balance;
    private Long relatedId;
    private String relatedType;
    private String remark;
    private Integer status;

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
