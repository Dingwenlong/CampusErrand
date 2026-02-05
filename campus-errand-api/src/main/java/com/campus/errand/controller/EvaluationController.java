package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.common.PageResult;
import com.campus.errand.common.Result;
import com.campus.errand.dto.EvaluationSubmitDTO;
import com.campus.errand.service.EvaluationService;
import com.campus.errand.util.UserContext;
import com.campus.errand.vo.EvaluationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评价管理", description = "任务评价相关接口")
@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @Autowired
    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @Operation(summary = "提交评价")
    @PostMapping("/submit")
    public Result<EvaluationVO> submit(@Valid @RequestBody EvaluationSubmitDTO submitDTO) {
        Long fromUserId = UserContext.getUserId();
        try {
            EvaluationVO evaluationVO = evaluationService.submitEvaluation(fromUserId, submitDTO);
            return Result.success(evaluationVO);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取任务的评价列表")
    @GetMapping("/task/{taskId}")
    public Result<List<EvaluationVO>> getTaskEvaluations(@PathVariable Long taskId) {
        List<EvaluationVO> list = evaluationService.getTaskEvaluations(taskId);
        return Result.success(list);
    }

    @Operation(summary = "获取我收到的评价")
    @GetMapping("/received")
    public Result<PageResult<EvaluationVO>> getReceivedEvaluations(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getUserId();
        IPage<EvaluationVO> page = evaluationService.getReceivedEvaluations(userId, current, size);
        PageResult<EvaluationVO> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return Result.success(result);
    }

    @Operation(summary = "获取我发出的评价")
    @GetMapping("/sent")
    public Result<PageResult<EvaluationVO>> getSentEvaluations(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getUserId();
        IPage<EvaluationVO> page = evaluationService.getSentEvaluations(userId, current, size);
        PageResult<EvaluationVO> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return Result.success(result);
    }

    @Operation(summary = "获取用户平均评分")
    @GetMapping("/average-rating/{userId}")
    public Result<Double> getUserAverageRating(@PathVariable Long userId) {
        Double averageRating = evaluationService.getUserAverageRating(userId);
        return Result.success(averageRating);
    }
}
