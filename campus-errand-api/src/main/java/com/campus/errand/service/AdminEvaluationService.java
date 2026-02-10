package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.errand.vo.EvaluationVO;

import java.util.Map;

public interface AdminEvaluationService {

    /**
     * 获取评价列表
     */
    IPage<EvaluationVO> getEvaluationList(Integer rating, String keyword, Long current, Long size);

    /**
     * 删除评价
     */
    boolean deleteEvaluation(Long evaluationId);

    /**
     * 获取评价统计
     */
    Map<String, Object> getEvaluationStats();
}
