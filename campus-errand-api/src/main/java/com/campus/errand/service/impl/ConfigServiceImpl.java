package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.entity.Config;
import com.campus.errand.mapper.ConfigMapper;
import com.campus.errand.service.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public boolean updateConfig(String key, String value) {
        Config config = baseMapper.selectByKey(key);
        if (config == null) {
            // 配置不存在，自动创建
            config = new Config();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(getConfigDescription(key));
            config.setCategory(getConfigCategory(key));
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            return save(config);
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

    /**
     * 根据配置key获取描述
     */
    private String getConfigDescription(String key) {
        return switch (key) {
            case "site_name" -> "系统名称";
            case "site_logo" -> "系统Logo URL";
            case "service_phone" -> "客服电话";
            case "service_email" -> "客服邮箱";
            case "min_reward" -> "最小赏金金额";
            case "max_reward" -> "最大赏金金额";
            case "auto_cancel_hours" -> "任务自动取消时间（小时）";
            case "timeout_warning_hours" -> "任务超时提醒时间（小时）";
            case "min_withdraw" -> "最低提现金额";
            case "max_withdraw" -> "最高提现金额";
            case "platform_fee" -> "平台抽成比例（%）";
            case "register_enabled" -> "用户注册开关";
            case "task_publish_enabled" -> "任务发布开关";
            case "verify_enabled" -> "实名认证开关";
            case "maintenance_mode" -> "系统维护开关";
            case "maintenance_message" -> "维护提示信息";
            case "user_agreement" -> "用户协议内容";
            case "privacy_policy" -> "隐私政策内容";
            case "user_agreement_update_time" -> "用户协议更新时间";
            case "privacy_policy_update_time" -> "隐私政策更新时间";
            default -> "系统配置";
        };
    }

    /**
     * 根据配置key获取分类
     */
    private String getConfigCategory(String key) {
        if (key.startsWith("site_") || key.startsWith("service_")) {
            return "basic";
        } else if (key.contains("reward") || key.contains("cancel") || key.contains("timeout")) {
            return "task";
        } else if (key.contains("withdraw") || key.contains("fee")) {
            return "wallet";
        } else if (key.contains("agreement") || key.contains("privacy")) {
            return "agreement";
        } else {
            return "other";
        }
    }
}
