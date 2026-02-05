package com.campus.errand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskPublishDTO {

    @NotNull(message = "任务类型不能为空")
    private Integer taskType;

    @NotBlank(message = "任务标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "取件地址不能为空")
    private String pickupAddress;

    private String pickupContact;
    private String pickupPhone;
    private Double pickupLongitude;
    private Double pickupLatitude;

    @NotBlank(message = "送达地址不能为空")
    private String deliveryAddress;

    private String deliveryContact;
    private String deliveryPhone;
    private Double deliveryLongitude;
    private Double deliveryLatitude;

    @NotNull(message = "赏金不能为空")
    private BigDecimal reward;

    private BigDecimal weightFee;
    private BigDecimal urgencyFee;
    private LocalDateTime expectTime;
    private LocalDateTime deadlineTime;
    private String remark;
    private String images;
    private Integer isUrgent;
}
