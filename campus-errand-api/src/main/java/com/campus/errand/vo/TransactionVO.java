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
    
    // 扩展字段
    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
    }
    
    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }
    
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}
