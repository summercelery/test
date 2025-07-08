package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AOP配置类
 * 启用AspectJ自动代理
 */
@Configuration
@EnableAspectJAutoProxy
public class AopConfig {
    // Spring Boot 默认会自动配置AOP
    // 这里显式声明以确保功能正常
} 