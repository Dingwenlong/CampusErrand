package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.entity.Config;
import com.campus.errand.mapper.ConfigMapper;
import com.campus.errand.service.ConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    @Override
    public String getConfigValue(String key) {
        Config config = baseMapper.selectByKey(key);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public String getConfigValue(String key, String defaultValue) {
        String value = getConfigValue(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean updateConfig(String key, String value) {
        Config config = baseMapper.selectByKey(key);
        if (config == null) {
            return false;
        }
        config.setConfigValue(value);
        config.setUpdateTime(LocalDateTime.now());
        return updateById(config);
    }

    @Override
    public List<Config> getConfigsByCategory(String category) {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getCategory, category);
        return list(wrapper);
    }
}
