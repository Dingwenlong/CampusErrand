package com.campus.errand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.errand.entity.UserWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {

    @Update("UPDATE tb_user_wallet SET frozen_amount = frozen_amount + #{amount}, version = version + 1, update_time = NOW() " +
            "WHERE user_id = #{userId} AND deleted = 0 AND balance - frozen_amount >= #{amount}")
    int freezeAmount(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Update("UPDATE tb_user_wallet SET frozen_amount = frozen_amount - #{amount}, version = version + 1, update_time = NOW() " +
            "WHERE user_id = #{userId} AND deleted = 0 AND frozen_amount >= #{amount}")
    int unfreezeAmount(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Update("UPDATE tb_user_wallet SET balance = balance - #{amount}, total_expense = total_expense + #{amount}, version = version + 1, update_time = NOW() " +
            "WHERE user_id = #{userId} AND deleted = 0 AND balance - frozen_amount >= #{amount}")
    int deductAvailableBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Update("UPDATE tb_user_wallet SET balance = balance + #{amount}, total_income = total_income + #{amount}, version = version + 1, update_time = NOW() " +
            "WHERE user_id = #{userId} AND deleted = 0")
    int addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
