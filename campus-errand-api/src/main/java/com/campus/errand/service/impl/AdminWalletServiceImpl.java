package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.Transaction;
import com.campus.errand.entity.User;
import com.campus.errand.mapper.TransactionMapper;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.service.AdminWalletService;
import com.campus.errand.vo.TransactionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminWalletServiceImpl implements AdminWalletService {

    private final TransactionMapper transactionMapper;
    private final UserMapper userMapper;

    @Autowired
    public AdminWalletServiceImpl(TransactionMapper transactionMapper, UserMapper userMapper) {
        this.transactionMapper = transactionMapper;
        this.userMapper = userMapper;
    }

    @Override
    public IPage<TransactionVO> getTransactions(Integer transactionType, Integer direction, Long userId,
                                                String startTime, String endTime, Long current, Long size) {
        Page<Transaction> page = new Page<>(current, size);
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();

        // 交易类型筛选
        if (transactionType != null) {
            wrapper.eq(Transaction::getTransactionType, transactionType);
        }

        // 交易方向筛选
        if (direction != null) {
            wrapper.eq(Transaction::getDirection, direction);
        }

        // 用户ID筛选
        if (userId != null) {
            wrapper.eq(Transaction::getUserId, userId);
        }

        // 时间范围筛选
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(Transaction::getCreateTime, LocalDateTime.parse(startTime, formatter));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(Transaction::getCreateTime, LocalDateTime.parse(endTime, formatter));
        }

        wrapper.orderByDesc(Transaction::getCreateTime);

        IPage<Transaction> transactionPage = transactionMapper.selectPage(page, wrapper);

        // 转换为VO
        List<TransactionVO> voList = transactionPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<TransactionVO> resultPage = new Page<>();
        resultPage.setCurrent(transactionPage.getCurrent());
        resultPage.setSize(transactionPage.getSize());
        resultPage.setTotal(transactionPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public Map<String, Object> getWalletStats() {
        Map<String, Object> result = new HashMap<>();

        // 总交易笔数
        Long totalCount = transactionMapper.selectCount(null);
        result.put("totalCount", totalCount);

        // 总收入金额
        LambdaQueryWrapper<Transaction> incomeWrapper = new LambdaQueryWrapper<>();
        incomeWrapper.eq(Transaction::getDirection, 1)
                     .eq(Transaction::getStatus, 1);
        List<Transaction> incomeList = transactionMapper.selectList(incomeWrapper);
        BigDecimal totalIncome = incomeList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalIncome", totalIncome);

        // 总支出金额
        LambdaQueryWrapper<Transaction> expenseWrapper = new LambdaQueryWrapper<>();
        expenseWrapper.eq(Transaction::getDirection, 2)
                      .eq(Transaction::getStatus, 1);
        List<Transaction> expenseList = transactionMapper.selectList(expenseWrapper);
        BigDecimal totalExpense = expenseList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalExpense", totalExpense);

        // 各类型交易统计
        int[] typeArray = {1, 2, 3, 4, 5};
        String[] typeNames = {"充值", "提现", "任务支付", "任务收入", "退款"};

        Map<String, Map<String, Object>> typeStats = new HashMap<>();
        for (int i = 0; i < typeArray.length; i++) {
            LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Transaction::getTransactionType, typeArray[i])
                   .eq(Transaction::getStatus, 1);
            List<Transaction> list = transactionMapper.selectList(wrapper);

            BigDecimal amount = list.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> stat = new HashMap<>();
            stat.put("count", list.size());
            stat.put("amount", amount);
            typeStats.put(typeNames[i], stat);
        }
        result.put("typeStats", typeStats);

        // 今日交易统计
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        LambdaQueryWrapper<Transaction> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Transaction::getCreateTime, todayStart)
                   .lt(Transaction::getCreateTime, todayEnd);
        Long todayCount = transactionMapper.selectCount(todayWrapper);
        result.put("todayCount", todayCount);

        // 今日收入
        LambdaQueryWrapper<Transaction> todayIncomeWrapper = new LambdaQueryWrapper<>();
        todayIncomeWrapper.eq(Transaction::getDirection, 1)
                         .eq(Transaction::getStatus, 1)
                         .ge(Transaction::getCreateTime, todayStart)
                         .lt(Transaction::getCreateTime, todayEnd);
        List<Transaction> todayIncomeList = transactionMapper.selectList(todayIncomeWrapper);
        BigDecimal todayIncome = todayIncomeList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("todayIncome", todayIncome);

        // 今日支出
        LambdaQueryWrapper<Transaction> todayExpenseWrapper = new LambdaQueryWrapper<>();
        todayExpenseWrapper.eq(Transaction::getDirection, 2)
                          .eq(Transaction::getStatus, 1)
                          .ge(Transaction::getCreateTime, todayStart)
                          .lt(Transaction::getCreateTime, todayEnd);
        List<Transaction> todayExpenseList = transactionMapper.selectList(todayExpenseWrapper);
        BigDecimal todayExpense = todayExpenseList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("todayExpense", todayExpense);

        return result;
    }

    private TransactionVO convertToVO(Transaction transaction) {
        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(transaction, vo);

        // 设置交易类型名称
        vo.setTransactionTypeName(getTransactionTypeName(transaction.getTransactionType()));

        // 设置交易方向名称
        vo.setDirectionName(getDirectionName(transaction.getDirection()));

        // 设置状态名称
        vo.setStatusName(getStatusName(transaction.getStatus()));

        // 查询用户信息
        User user = userMapper.selectById(transaction.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
            vo.setUserPhone(user.getPhone());
        }

        return vo;
    }

    private String getTransactionTypeName(Integer type) {
        switch (type) {
            case 1: return "充值";
            case 2: return "提现";
            case 3: return "任务支付";
            case 4: return "任务收入";
            case 5: return "退款";
            default: return "未知";
        }
    }

    private String getDirectionName(Integer direction) {
        switch (direction) {
            case 1: return "收入";
            case 2: return "支出";
            default: return "未知";
        }
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "处理中";
            case 1: return "成功";
            case 2: return "失败";
            default: return "未知";
        }
    }
}
