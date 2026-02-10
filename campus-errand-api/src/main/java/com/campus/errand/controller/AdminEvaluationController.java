package com.campus.errand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.common.PageResult;
import com.campus.errand.common.Result;
import com.campus.errand.service.AdminEvaluationService;
import com.campus.errand.vo.EvaluationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "后台-评价管理", description = "后台评价管理接口")
@RestController
@RequestMapping("/admin/evaluation")
public class AdminEvaluationController {

    private final AdminEvaluationService adminEvaluationService;

    @Autowired
    public AdminEvaluationController(AdminEvaluationService adminEvaluationService) {
        this.adminEvaluationService = adminEvaluationService;
    }

    @Operation(summary = "获取评价列表")
    @GetMapping("/list")
    public Result<PageResult<EvaluationVO>> list(
            @Parameter(description = "评分：1-5") @RequestParam(required = false) Integer rating,
            @Parameter(description = "关键词（评价内容）") @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<EvaluationVO> page = adminEvaluationService.getEvaluationList(rating, keyword, current, size);
        PageResult<EvaluationVO> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return Result.success(result);
    }

    @Operation(summary = "删除评价")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = adminEvaluationService.deleteEvaluation(id);
        if (!success) {
            return Result.error("删除评价失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "获取评价统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = adminEvaluationService.getEvaluationStats();
        return Result.success(data);
    }
}
