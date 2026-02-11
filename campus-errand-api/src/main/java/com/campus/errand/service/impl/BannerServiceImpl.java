package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.entity.Banner;
import com.campus.errand.mapper.BannerMapper;
import com.campus.errand.service.BannerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<Banner> getActiveBanners() {
        return baseMapper.selectActiveBanners();
    }

    @Override
    public boolean addBanner(Banner banner) {
        banner.setCreateTime(LocalDateTime.now());
        banner.setUpdateTime(LocalDateTime.now());
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        return save(banner);
    }

    @Override
    public boolean updateBanner(Banner banner) {
        banner.setUpdateTime(LocalDateTime.now());
        return updateById(banner);
    }

    @Override
    public boolean deleteBanner(Long id) {
        return removeById(id);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Banner banner = getById(id);
        if (banner == null) {
            return false;
        }
        banner.setStatus(status);
        banner.setUpdateTime(LocalDateTime.now());
        return updateById(banner);
    }
}
