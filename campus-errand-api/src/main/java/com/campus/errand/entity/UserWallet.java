package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_user_wallet")
public class UserWallet extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private String payPassword;
}
