package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String openid;
    private String unionid;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer gender;
    private Integer userType;
    private Integer status;
    private Integer isVerified;
    private String realName;
    private String idCard;
    private Integer verifyStatus;
    private String verifyRejectReason;
    private String idCardFrontImage;
    private String idCardBackImage;
    private java.time.LocalDateTime verifySubmitTime;
    private java.time.LocalDateTime verifyTime;
    private Long verifyAdminId;
    private String studentId;
    private String schoolName;
    private Integer creditScore;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer cancelledOrders;
}
