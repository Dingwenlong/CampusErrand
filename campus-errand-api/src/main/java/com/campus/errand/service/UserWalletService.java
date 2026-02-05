package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.UserWallet;

public interface UserWalletService extends IService<UserWallet> {

    UserWallet getByUserId(Long userId);
}
