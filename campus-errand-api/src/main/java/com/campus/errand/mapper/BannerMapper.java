package com.campus.errand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.errand.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    @Select("SELECT * FROM tb_banner WHERE status = 1 ORDER BY sort_order ASC, id DESC")
    List<Banner> selectActiveBanners();
}
