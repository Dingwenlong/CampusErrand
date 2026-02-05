package com.campus.errand.service;

import java.util.Map;

public interface AdminService {

    /**
     * 管理员登录
     * @param username 用户名
     * @param password 密码
     * @return JWT Token
     */
    String login(String username, String password);

    /**
     * 获取仪表盘数据
     * @return 统计数据
     */
    Map<String, Object> getDashboardData();

    /**
     * 获取用户列表
     * @param current 当前页
     * @param size 每页大小
     * @param keyword 关键词
     * @return 用户列表
     */
    Map<String, Object> getUserList(Long current, Long size, String keyword);

    /**
     * 修改用户状态
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, Integer status);
}
