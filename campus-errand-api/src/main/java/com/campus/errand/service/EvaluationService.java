package com.campus.errand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.dto.EvaluationSubmitDTO;
import com.campus.errand.entity.Evaluation;
import com.campus.errand.vo.EvaluationVO;

import java.util.List;

public interface EvaluationService extends IService<Evaluation> {

    /**
     * 提交评价
     * @param fromUserId 评价者ID
     * @param submitDTO 评价信息
     * @return 评价VO
     */
    EvaluationVO submitEvaluation(Long fromUserId, EvaluationSubmitDTO submitDTO);

    /**
     * 获取任务的评价列表
     * @param taskId 任务ID
     * @return 评价列表
     */
    List<EvaluationVO> getTaskEvaluations(Long taskId);

    /**
     * 获取用户收到的评价
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页评价列表
     */
    IPage<EvaluationVO> getReceivedEvaluations(Long userId, Long current, Long size);

    /**
     * 获取用户发出的评价
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页评价列表
     */
    IPage<EvaluationVO> getSentEvaluations(Long userId, Long current, Long size);

    /**
     * 计算用户平均评分
     * @param userId 用户ID
     * @return 平均评分
     */
    Double getUserAverageRating(Long userId);
}
