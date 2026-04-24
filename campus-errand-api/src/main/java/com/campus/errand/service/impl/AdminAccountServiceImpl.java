package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.entity.AdminAccount;
import com.campus.errand.mapper.AdminAccountMapper;
import com.campus.errand.service.AdminAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminAccountServiceImpl extends ServiceImpl<AdminAccountMapper, AdminAccount> implements AdminAccountService {

    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.init-username:admin}")
    private String initUsername;

    @Value("${app.admin.init-password:}")
    private String initPassword;

    @Value("${app.admin.init-nickname:系统管理员}")
    private String initNickname;

    public AdminAccountServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminAccount getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<AdminAccount>().eq(AdminAccount::getUsername, username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminAccount authenticate(String username, String password) {
        AdminAccount account = getByUsername(username);
        if (account == null || account.getStatus() == null || account.getStatus() != 1) {
            return null;
        }
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            return null;
        }
        account.setLastLoginTime(LocalDateTime.now());
        updateById(account);
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeDefaultAdmin() {
        if (initPassword == null || initPassword.isBlank()) {
            return;
        }
        if (getByUsername(initUsername) != null) {
            return;
        }
        AdminAccount account = new AdminAccount();
        account.setUsername(initUsername);
        account.setPasswordHash(passwordEncoder.encode(initPassword));
        account.setNickname(initNickname);
        account.setRole("admin");
        account.setStatus(1);
        account.setForcePasswordChange(1);
        save(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long adminId, String oldPassword, String newPassword) {
        AdminAccount account = getById(adminId);
        if (account == null || !passwordEncoder.matches(oldPassword, account.getPasswordHash())) {
            return false;
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setForcePasswordChange(0);
        return updateById(account);
    }
}
