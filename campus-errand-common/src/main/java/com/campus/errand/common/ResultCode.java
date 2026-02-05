package com.campus.errand.common;

public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // 认证相关
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "拒绝访问"),
    TOKEN_EXPIRED(401001, "Token已过期"),
    TOKEN_INVALID(401002, "Token无效"),

    // 参数相关
    PARAM_ERROR(400001, "参数错误"),
    PARAM_EMPTY(400002, "参数为空"),

    // 用户相关
    USER_NOT_EXIST(100001, "用户不存在"),
    USER_ALREADY_EXIST(100002, "用户已存在"),
    USER_PASSWORD_ERROR(100003, "密码错误"),
    USER_DISABLED(100004, "用户已被禁用"),
    USER_NOT_VERIFIED(100005, "用户未实名认证"),

    // 订单相关
    ORDER_NOT_EXIST(200001, "订单不存在"),
    ORDER_STATUS_ERROR(200002, "订单状态错误"),
    ORDER_ALREADY_ACCEPTED(200003, "订单已被接单"),
    ORDER_NOT_BELONG_TO_USER(200004, "订单不属于当前用户"),

    // 支付相关
    INSUFFICIENT_BALANCE(300001, "余额不足"),
    PAYMENT_FAILED(300002, "支付失败"),

    // 系统相关
    SYSTEM_ERROR(500001, "系统错误"),
    SERVICE_BUSY(500002, "服务繁忙，请稍后重试");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
