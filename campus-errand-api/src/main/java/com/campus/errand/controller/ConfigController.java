package com.campus.errand.controller;

import com.campus.errand.common.Result;
import com.campus.errand.entity.Config;
import com.campus.errand.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统配置", description = "系统配置管理接口")
@RestController
@RequestMapping("/config")
public class ConfigController {

    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @Operation(summary = "获取所有配置")
    @GetMapping("/list")
    public Result<List<Config>> list() {
        List<Config> list = configService.list();
        return Result.success(list);
    }

    @Operation(summary = "根据分类获取配置")
    @GetMapping("/category/{category}")
    public Result<List<Config>> getByCategory(@PathVariable String category) {
        List<Config> list = configService.getConfigsByCategory(category);
        return Result.success(list);
    }

    @Operation(summary = "根据key获取配置")
    @GetMapping("/{key}")
    public Result<String> getByKey(@PathVariable String key) {
        String value = configService.getConfigValue(key);
        return Result.success(value);
    }

    @Operation(summary = "更新配置")
    @PutMapping("/{key}")
    public Result<Boolean> update(@PathVariable String key, @RequestParam String value) {
        boolean success = configService.updateConfig(key, value);
        if (!success) {
            return Result.error("配置不存在或更新失败");
        }
        return Result.success(true);
    }

    @Operation(summary = "批量更新配置")
    @PutMapping("/batch")
    public Result<Boolean> batchUpdate(@RequestBody List<Config> configs) {
        boolean success = configService.updateBatchById(configs);
        return Result.success(success);
    }

    @Operation(summary = "保存配置（支持新增和更新）")
    @PostMapping("/save")
    public Result<Boolean> saveConfig(@RequestBody Map<String, String> configMap) {
        boolean allSuccess = true;
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            boolean success = configService.updateConfig(entry.getKey(), entry.getValue());
            if (!success) {
                allSuccess = false;
            }
        }
        if (!allSuccess) {
            return Result.error("部分配置保存失败");
        }
        return Result.success(true);
    }
}
