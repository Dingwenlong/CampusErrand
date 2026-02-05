package com.campus.errand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.errand.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT COUNT(*) FROM tb_message WHERE user_id = #{userId} AND is_read = 0")
    int countUnreadByUserId(Long userId);

    @Update("UPDATE tb_message SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(Long userId);
}
