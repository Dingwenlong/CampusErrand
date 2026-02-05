package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.User;
import com.campus.errand.mapper.TaskMapper;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.service.AdminService;
import com.campus.errand.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    // 默认管理员账号密码（实际应该存储在数据库中并加密）
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final JwtUtil jwtUtil;

    @Autowired
    public AdminServiceImpl(UserMapper userMapper, TaskMapper taskMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String login(String username, String password) {
        // 简单的账号密码验证
        if (DEFAULT_USERNAME.equals(username) && DEFAULT_PASSWORD.equals(password)) {
            // 生成管理员Token（使用特殊标识）
            return jwtUtil.generateToken(0L, "admin_openid");
        }
        return null;
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 用户统计
        Long userCount = userMapper.selectCount(null);
        data.put("userCount", userCount);

        // 任务统计
        Long taskCount = taskMapper.selectCount(null);
        data.put("taskCount", taskCount);

        // 订单统计（这里用任务数代替，实际应该查询订单表）
        data.put("orderCount", taskCount);

        // 交易总额（模拟数据）
        data.put("totalAmount", 12580.50);

        // 今日数据（模拟）
        data.put("todayUserCount", 5);
        data.put("todayTaskCount", 12);
        data.put("todayOrderCount", 8);
        data.put("todayAmount", 368.00);

        return data;
    }

    @Override
    public Map<String, Object> getUserList(Long current, Long size, String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getNickname, keyword)
                   .or()
                   .like(User::getPhone, keyword);
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("records", userPage.getRecords());
        result.put("total", userPage.getTotal());
        result.put("size", userPage.getSize());
        result.put("current", userPage.getCurrent());
        result.put("pages", userPage.getPages());

        return result;
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }
}
