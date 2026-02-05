package com.campus.errand.vo;

public class LoginVO {

    private String token;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer isNewUser;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Integer getIsNewUser() {
        return isNewUser;
    }

    public void setIsNewUser(Integer isNewUser) {
        this.isNewUser = isNewUser;
    }
}
