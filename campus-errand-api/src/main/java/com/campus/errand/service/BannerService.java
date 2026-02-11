package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.Banner;

import java.util.List;

public interface BannerService extends IService<Banner> {

    /**
     * 获取启用的轮播图列表
     */
    List<Banner> getActiveBanners();

    /**
     * 新增轮播图
     */
    boolean addBanner(Banner banner);

    /**
     * 更新轮播图
     */
    boolean updateBanner(Banner banner);

    /**
     * 删除轮播图
     */
    boolean deleteBanner(Long id);

    /**
     * 更新轮播图状态
     */
    boolean updateStatus(Long id, Integer status);
}
