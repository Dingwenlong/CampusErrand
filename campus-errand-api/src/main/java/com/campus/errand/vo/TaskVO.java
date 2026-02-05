package com.campus.errand.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskVO {

    private Long id;
    private Long userId;
    private Integer taskType;
    private String taskTypeName;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadlineTime;
    private Integer status;
    private String statusName;
    private Long runnerId;
    private String runnerName;
    private String runnerAvatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickupTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
    private String remark;
    private String images;
    private Integer isUrgent;

    // 发布者信息
    private String publisherName;
    private String publisherAvatar;
    private Integer publisherCreditScore;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
