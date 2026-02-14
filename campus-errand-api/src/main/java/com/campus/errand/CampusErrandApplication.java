package com.campus.errand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.campus.errand"})
@EnableScheduling
public class CampusErrandApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusErrandApplication.class, args);
    }
}
