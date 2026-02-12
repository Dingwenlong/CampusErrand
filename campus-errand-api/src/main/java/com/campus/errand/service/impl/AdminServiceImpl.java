package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.errand.entity.Task;
import com.campus.errand.entity.Transaction;
import com.campus.errand.entity.User;
import com.campus.errand.mapper.TaskMapper;
import com.campus.errand.mapper.TransactionMapper;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final TransactionMapper transactionMapper;

    @Autowired
    public AdminServiceImpl(UserMapper userMapper, TaskMapper taskMapper, TransactionMapper transactionMapper) {
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();

        // 总用户数
        Long userCount = userMapper.selectCount(null);
        result.put("userCount", userCount);

        // 总任务数
        Long taskCount = taskMapper.selectCount(null);
        result.put("taskCount", taskCount);

        // 交易总额
        LambdaQueryWrapper<Transaction> amountWrapper = new LambdaQueryWrapper<>();
        amountWrapper.eq(Transaction::getDirection, 1)
                    .eq(Transaction::getStatus, 1);
        List<Transaction> incomeTransactions = transactionMapper.selectList(amountWrapper);
        BigDecimal totalAmount = incomeTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalAmount", totalAmount);

        return result;
    }

    @Override
    public Map<String, Object> getTaskStatusStats() {
        Map<String, Object> result = new HashMap<>();

        // 各状态任务数量
        int[] statusArray = {0, 1, 2, 3, 4, 5, 6};
        String[] statusNames = {"待接单", "已接单", "待取件", "配送中", "待确认", "已完成", "已取消"};

        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < statusArray.length; i++) {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getStatus, statusArray[i]);
            Long count = taskMapper.selectCount(wrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", count);
            item.put("status", statusArray[i]);
            data.add(item);
        }

        result.put("data", data);
        return result;
    }

    @Override
    public Map<String, Object> getAmountTrend() {
        Map<String, Object> result = new HashMap<>();

        // 获取最近7天的日期
        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            // 查询当天的交易额
            LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Transaction::getDirection, 1)
                   .eq(Transaction::getStatus, 1)
                   .ge(Transaction::getCreateTime, dayStart)
                   .lt(Transaction::getCreateTime, dayEnd);
            List<Transaction> transactions = transactionMapper.selectList(wrapper);

            BigDecimal dayAmount = transactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amounts.add(dayAmount);
        }

        result.put("dates", dates);
        result.put("amounts", amounts);
        return result;
    }

    @Override
    public Map<String, Object> getUserGrowth() {
        Map<String, Object> result = new HashMap<>();

        // 获取最近7天的日期
        List<String> dates = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> totalUsers = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            // 查询当天新增用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(User::getCreateTime, dayStart)
                   .lt(User::getCreateTime, dayEnd);
            Long dayNewUsers = userMapper.selectCount(wrapper);
            newUsers.add(dayNewUsers);

            // 查询截止到当天的总用户数
            LambdaQueryWrapper<User> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.lt(User::getCreateTime, dayEnd);
            Long dayTotalUsers = userMapper.selectCount(totalWrapper);
            totalUsers.add(dayTotalUsers);
        }

        result.put("dates", dates);
        result.put("newUsers", newUsers);
        result.put("totalUsers", totalUsers);
        return result;
    }
}
