package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_evaluation")
public class Evaluation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long taskId;
    private Long fromUserId;
    private Long toUserId;
    private Integer userType;
    private Integer rating;
    private String content;
    private String tags;
    private Integer isAnonymous;
}
