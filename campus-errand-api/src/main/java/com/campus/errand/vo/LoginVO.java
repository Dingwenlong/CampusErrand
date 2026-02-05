package com.campus.errand.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer isNewUser;
}
