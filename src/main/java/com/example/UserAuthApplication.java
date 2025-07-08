package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 用户认证系统主应用类
 */
@SpringBootApplication
@EnableScheduling
public class UserAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAuthApplication.class, args);
    }
} 