package com.campus.errand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.errand.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
