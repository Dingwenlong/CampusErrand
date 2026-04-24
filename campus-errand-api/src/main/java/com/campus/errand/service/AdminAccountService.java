package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.errand.entity.AdminAccount;

public interface AdminAccountService extends IService<AdminAccount> {

    AdminAccount authenticate(String username, String password);

    AdminAccount getByUsername(String username);

    void initializeDefaultAdmin();

    boolean changePassword(Long adminId, String oldPassword, String newPassword);
}
