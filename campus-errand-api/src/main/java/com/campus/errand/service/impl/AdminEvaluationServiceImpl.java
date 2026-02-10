package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.Evaluation;
import com.campus.errand.entity.Task;
import com.campus.errand.entity.User;
import com.campus.errand.mapper.EvaluationMapper;
import com.campus.errand.mapper.TaskMapper;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.service.AdminEvaluationService;
import com.campus.errand.vo.EvaluationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminEvaluationServiceImpl implements AdminEvaluationService {

    private final EvaluationMapper evaluationMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;

    @Autowired
    public AdminEvaluationServiceImpl(EvaluationMapper evaluationMapper, UserMapper userMapper, TaskMapper taskMapper) {
        this.evaluationMapper = evaluationMapper;
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public IPage<EvaluationVO> getEvaluationList(Integer rating, String keyword, Long current, Long size) {
        Page<Evaluation> page = new Page<>(current, size);
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();

        // 评分筛选
        if (rating != null) {
            wrapper.eq(Evaluation::getRating, rating);
        }

        // 关键词搜索（评价内容）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Evaluation::getContent, keyword.trim());
        }

        wrapper.orderByDesc(Evaluation::getCreateTime);

        IPage<Evaluation> evaluationPage = evaluationMapper.selectPage(page, wrapper);

        // 转换为VO
        List<EvaluationVO> voList = evaluationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<EvaluationVO> resultPage = new Page<>();
        resultPage.setCurrent(evaluationPage.getCurrent());
        resultPage.setSize(evaluationPage.getSize());
        resultPage.setTotal(evaluationPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public boolean deleteEvaluation(Long evaluationId) {
        return evaluationMapper.deleteById(evaluationId) > 0;
    }

    @Override
    public Map<String, Object> getEvaluationStats() {
        Map<String, Object> result = new HashMap<>();

        // 总评价数
        Long totalCount = evaluationMapper.selectCount(null);
        result.put("totalCount", totalCount);

        // 平均评分
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        List<Evaluation> allEvaluations = evaluationMapper.selectList(wrapper);
        
        if (!allEvaluations.isEmpty()) {
            double avgRating = allEvaluations.stream()
                    .mapToInt(Evaluation::getRating)
                    .average()
                    .orElse(0.0);
            result.put("averageRating", BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
        } else {
            result.put("averageRating", 5.0);
        }

        // 各评分数量
        Map<Integer, Long> ratingCount = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            LambdaQueryWrapper<Evaluation> ratingWrapper = new LambdaQueryWrapper<>();
            ratingWrapper.eq(Evaluation::getRating, i);
            Long count = evaluationMapper.selectCount(ratingWrapper);
            ratingCount.put(i, count);
        }
        result.put("ratingCount", ratingCount);

        // 今日新增评价
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LambdaQueryWrapper<Evaluation> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Evaluation::getCreateTime, todayStart)
                   .lt(Evaluation::getCreateTime, todayEnd);
        Long todayCount = evaluationMapper.selectCount(todayWrapper);
        result.put("todayCount", todayCount);

        return result;
    }

    private EvaluationVO convertToVO(Evaluation evaluation) {
        EvaluationVO vo = new EvaluationVO();
        BeanUtils.copyProperties(evaluation, vo);

        // 查询评价人信息
        User fromUser = userMapper.selectById(evaluation.getFromUserId());
        if (fromUser != null) {
            vo.setFromUserName(fromUser.getNickname());
            vo.setFromUserAvatar(fromUser.getAvatar());
        }

        // 查询被评价人信息
        User toUser = userMapper.selectById(evaluation.getToUserId());
        if (toUser != null) {
            vo.setToUserName(toUser.getNickname());
            vo.setToUserAvatar(toUser.getAvatar());
        }

        // 查询任务信息
        Task task = taskMapper.selectById(evaluation.getTaskId());
        if (task != null) {
            vo.setTaskTitle(task.getTitle());
        }

        return vo;
    }
}
