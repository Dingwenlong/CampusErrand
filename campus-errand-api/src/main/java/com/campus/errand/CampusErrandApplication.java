package com.campus.errand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.campus.errand"})
public class CampusErrandApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusErrandApplication.class, args);
    }
}
