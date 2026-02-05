package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.dto.EvaluationSubmitDTO;
import com.campus.errand.entity.Evaluation;
import com.campus.errand.entity.Task;
import com.campus.errand.entity.User;
import com.campus.errand.mapper.EvaluationMapper;
import com.campus.errand.mapper.TaskMapper;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.service.EvaluationService;
import com.campus.errand.service.UserService;
import com.campus.errand.vo.EvaluationVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Autowired
    public EvaluationServiceImpl(TaskMapper taskMapper, UserMapper userMapper, UserService userService, ObjectMapper objectMapper) {
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationVO submitEvaluation(Long fromUserId, EvaluationSubmitDTO submitDTO) {
        // 1. 验证任务是否存在且已完成
        Task task = taskMapper.selectById(submitDTO.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (task.getStatus() != 5) {
            throw new RuntimeException("任务未完成，不能评价");
        }

        // 2. 验证评价者是否是任务的参与方
        boolean isPublisher = task.getUserId().equals(fromUserId);
        boolean isRunner = task.getRunnerId() != null && task.getRunnerId().equals(fromUserId);
        if (!isPublisher && !isRunner) {
            throw new RuntimeException("无权评价该任务");
        }

        // 3. 验证被评价者是否是任务的另一方
        Long toUserId = submitDTO.getToUserId();
        if (isPublisher && !toUserId.equals(task.getRunnerId())) {
            throw new RuntimeException("只能评价接单者");
        }
        if (isRunner && !toUserId.equals(task.getUserId())) {
            throw new RuntimeException("只能评价发单者");
        }

        // 4. 检查是否已评价过
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getTaskId, submitDTO.getTaskId())
               .eq(Evaluation::getFromUserId, fromUserId);
        if (count(wrapper) > 0) {
            throw new RuntimeException("已评价过该任务");
        }

        // 5. 创建评价记录
        Evaluation evaluation = new Evaluation();
        evaluation.setTaskId(submitDTO.getTaskId());
        evaluation.setFromUserId(fromUserId);
        evaluation.setToUserId(toUserId);
        evaluation.setUserType(isPublisher ? 1 : 2); // 1-发单者 2-跑腿员
        evaluation.setRating(submitDTO.getRating());
        evaluation.setContent(submitDTO.getContent());

        // 处理标签
        if (submitDTO.getTags() != null && !submitDTO.getTags().isEmpty()) {
            try {
                evaluation.setTags(objectMapper.writeValueAsString(submitDTO.getTags()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("标签格式错误");
            }
        }

        evaluation.setIsAnonymous(submitDTO.getIsAnonymous() != null ? submitDTO.getIsAnonymous() : 0);
        evaluation.setCreateTime(LocalDateTime.now());

        save(evaluation);

        // 6. 更新被评价者信用分
        updateCreditScore(toUserId, submitDTO.getRating());

        return convertToVO(evaluation);
    }

    /**
     * 更新用户信用分
     */
    private void updateCreditScore(Long userId, Integer rating) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        int scoreChange;
        switch (rating) {
            case 5: scoreChange = 2; break;
            case 4: scoreChange = 1; break;
            case 3: scoreChange = 0; break;
            case 2: scoreChange = -2; break;
            case 1: scoreChange = -5; break;
            default: scoreChange = 0;
        }

        int newScore = user.getCreditScore() + scoreChange;
        // 信用分范围：0-100
        if (newScore < 0) newScore = 0;
        if (newScore > 100) newScore = 100;

        user.setCreditScore(newScore);
        userMapper.updateById(user);
    }

    @Override
    public List<EvaluationVO> getTaskEvaluations(Long taskId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getTaskId, taskId)
               .orderByDesc(Evaluation::getCreateTime);

        List<Evaluation> list = list(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<EvaluationVO> getReceivedEvaluations(Long userId, Long current, Long size) {
        Page<Evaluation> page = new Page<>(current, size);
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getToUserId, userId)
               .orderByDesc(Evaluation::getCreateTime);

        IPage<Evaluation> evaluationPage = page(page, wrapper);

        List<EvaluationVO> voList = evaluationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<EvaluationVO> voPage = new Page<>();
        voPage.setCurrent(evaluationPage.getCurrent());
        voPage.setSize(evaluationPage.getSize());
        voPage.setTotal(evaluationPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<EvaluationVO> getSentEvaluations(Long userId, Long current, Long size) {
        Page<Evaluation> page = new Page<>(current, size);
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getFromUserId, userId)
               .orderByDesc(Evaluation::getCreateTime);

        IPage<Evaluation> evaluationPage = page(page, wrapper);

        List<EvaluationVO> voList = evaluationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<EvaluationVO> voPage = new Page<>();
        voPage.setCurrent(evaluationPage.getCurrent());
        voPage.setSize(evaluationPage.getSize());
        voPage.setTotal(evaluationPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public Double getUserAverageRating(Long userId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getToUserId, userId);

        List<Evaluation> list = list(wrapper);
        if (list.isEmpty()) {
            return 5.0; // 默认5分
        }

        double sum = list.stream().mapToInt(Evaluation::getRating).sum();
        return sum / list.size();
    }

    private EvaluationVO convertToVO(Evaluation evaluation) {
        EvaluationVO vo = new EvaluationVO();
        BeanUtils.copyProperties(evaluation, vo);

        // 设置评价者类型名称
        vo.setUserTypeName(evaluation.getUserType() == 1 ? "发单者" : "跑腿员");

        // 设置评价者信息（如果不是匿名）
        if (evaluation.getIsAnonymous() == null || evaluation.getIsAnonymous() == 0) {
            User fromUser = userMapper.selectById(evaluation.getFromUserId());
            if (fromUser != null) {
                vo.setFromUserName(fromUser.getNickname());
                vo.setFromUserAvatar(fromUser.getAvatar());
            }
        } else {
            vo.setFromUserName("匿名用户");
        }

        // 解析标签
        if (evaluation.getTags() != null && !evaluation.getTags().isEmpty()) {
            try {
                vo.setTags(objectMapper.readValue(evaluation.getTags(), List.class));
            } catch (JsonProcessingException e) {
                // 忽略解析错误
            }
        }

        return vo;
    }
}
