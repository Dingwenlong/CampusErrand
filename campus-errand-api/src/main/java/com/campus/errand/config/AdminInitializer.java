package com.campus.errand.config;

import com.campus.errand.service.AdminAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final AdminAccountService adminAccountService;

    public AdminInitializer(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @Override
    public void run(String... args) {
        adminAccountService.initializeDefaultAdmin();
    }
}
