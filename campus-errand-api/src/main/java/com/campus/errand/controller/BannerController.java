package com.campus.errand.controller;

import com.campus.errand.common.Result;
import com.campus.errand.entity.Banner;
import com.campus.errand.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "轮播图管理", description = "首页轮播图相关接口")
@RestController
@RequestMapping("/banner")
public class BannerController {

    private final BannerService bannerService;

    @Autowired
    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @Operation(summary = "获取轮播图列表（前台）")
    @GetMapping("/list")
    public Result<List<Banner>> list() {
        List<Banner> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }

    @Operation(summary = "获取所有轮播图（后台）")
    @GetMapping("/all")
    public Result<List<Banner>> getAll() {
        List<Banner> banners = bannerService.list();
        return Result.success(banners);
    }

    @Operation(summary = "获取轮播图详情")
    @GetMapping("/{id}")
    public Result<Banner> getById(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }
        return Result.success(banner);
    }

    @Operation(summary = "新增轮播图")
    @PostMapping
    public Result<Boolean> add(@RequestBody Banner banner) {
        boolean success = bannerService.addBanner(banner);
        if (success) {
            return Result.success(true);
        }
        return Result.error("添加失败");
    }

    @Operation(summary = "更新轮播图")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        boolean success = bannerService.updateBanner(banner);
        if (success) {
            return Result.success(true);
        }
        return Result.error("更新失败");
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = bannerService.deleteBanner(id);
        if (success) {
            return Result.success(true);
        }
        return Result.error("删除失败");
    }

    @Operation(summary = "更新轮播图状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = bannerService.updateStatus(id, status);
        if (success) {
            return Result.success(true);
        }
        return Result.error("状态更新失败");
    }
}
