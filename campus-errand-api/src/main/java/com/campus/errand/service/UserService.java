package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.User;

public interface UserService extends IService<User> {

    User getByOpenid(String openid);
}
