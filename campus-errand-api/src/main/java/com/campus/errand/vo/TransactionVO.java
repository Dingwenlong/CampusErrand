package com.campus.errand.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionVO {

    private Long id;
    private String transactionNo;
    private Long userId;
    private String userName;
    private String userPhone;
    private Integer direction;
    private String directionName;
    private Integer transactionType;
    private String transactionTypeName;
    private BigDecimal amount;
    private BigDecimal balance;
    private Long relatedId;
    private Integer status;
    private String statusName;
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
