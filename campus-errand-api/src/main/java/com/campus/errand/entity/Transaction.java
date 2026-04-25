package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
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
}
