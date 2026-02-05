package com.campus.errand.util;

public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> OPENID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setOpenid(String openid) {
        OPENID.set(openid);
    }

    public static String getOpenid() {
        return OPENID.get();
    }

    public static void clear() {
        USER_ID.remove();
        OPENID.remove();
    }
}
