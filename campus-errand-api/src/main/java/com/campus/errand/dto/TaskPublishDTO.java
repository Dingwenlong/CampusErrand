package com.campus.errand.dto;

import com.campus.errand.validator.ValidTaskTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ValidTaskTime
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadlineTime;
    private String remark;
    private String images;
    private Integer isUrgent;

    @NotBlank(message = "支付密码不能为空")
    private String payPassword;

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getPickupContact() {
        return pickupContact;
    }

    public void setPickupContact(String pickupContact) {
        this.pickupContact = pickupContact;
    }

    public String getPickupPhone() {
        return pickupPhone;
    }

    public void setPickupPhone(String pickupPhone) {
        this.pickupPhone = pickupPhone;
    }

    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(Double pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(Double pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryContact() {
        return deliveryContact;
    }

    public void setDeliveryContact(String deliveryContact) {
        this.deliveryContact = deliveryContact;
    }

    public String getDeliveryPhone() {
        return deliveryPhone;
    }

    public void setDeliveryPhone(String deliveryPhone) {
        this.deliveryPhone = deliveryPhone;
    }

    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(Double deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }

    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(Double deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }

    public BigDecimal getWeightFee() {
        return weightFee;
    }

    public void setWeightFee(BigDecimal weightFee) {
        this.weightFee = weightFee;
    }

    public BigDecimal getUrgencyFee() {
        return urgencyFee;
    }

    public void setUrgencyFee(BigDecimal urgencyFee) {
        this.urgencyFee = urgencyFee;
    }

    public LocalDateTime getExpectTime() {
        return expectTime;
    }

    public void setExpectTime(LocalDateTime expectTime) {
        this.expectTime = expectTime;
    }

    public LocalDateTime getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(LocalDateTime deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getIsUrgent() {
        return isUrgent;
    }

    public void setIsUrgent(Integer isUrgent) {
        this.isUrgent = isUrgent;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }
}
