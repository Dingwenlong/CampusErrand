package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task")
public class Task extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Integer taskType;
    private String title;
    private String description;
    private String pickupAddress;
    private String pickupContact;
    private String pickupPhone;
    private Double pickupLongitude;
    private Double pickupLatitude;
    private String deliveryAddress;
    private String deliveryContact;
    private String deliveryPhone;
    private Double deliveryLongitude;
    private Double deliveryLatitude;
    private BigDecimal reward;
    private BigDecimal weightFee;
    private BigDecimal urgencyFee;
    private BigDecimal totalAmount;
    private LocalDateTime expectTime;
    private LocalDateTime deadlineTime;
    private Integer status;
    private Long runnerId;
    private LocalDateTime acceptTime;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime completeTime;
    private String remark;
    private String images;
    private Integer isUrgent;
}
