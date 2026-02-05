package com.campus.errand.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginDTO {

    @NotBlank(message = "code不能为空")
    private String code;

    private String nickname;
    private String avatar;
    private Integer gender;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }
}
