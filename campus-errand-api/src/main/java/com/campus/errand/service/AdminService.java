package com.campus.errand.service;

import java.util.Map;

public interface AdminService {

    /**
     * 获取仪表盘数据
     */
    Map<String, Object> getDashboardData();

    /**
     * 获取任务状态分布
     */
    Map<String, Object> getTaskStatusStats();

    /**
     * 获取交易趋势数据
     */
    Map<String, Object> getAmountTrend();

    /**
     * 获取用户增长趋势
     */
    Map<String, Object> getUserGrowth();
}
