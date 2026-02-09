package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.Config;

import java.util.List;

public interface ConfigService extends IService<Config> {

    /**
     * 根据key获取配置值
     */
    String getConfigValue(String key);

    /**
     * 根据key获取配置值，如果不存在返回默认值
     */
    String getConfigValue(String key, String defaultValue);

    /**
     * 更新配置值
     */
    boolean updateConfig(String key, String value);

    /**
     * 根据分类获取配置列表
     */
    List<Config> getConfigsByCategory(String category);
}
