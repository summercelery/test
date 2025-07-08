package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 电话服务类
 */
@Slf4j
@Service
public class PhoneService {

    /**
     * 拨打电话
     */
    public void makeCall(String phoneNumber, String message) {
        try {
            // TODO: 集成实际的电话服务商API
            log.info("拨打电话到 {}: {}", phoneNumber, message);
            
            // 模拟拨打电话成功
            // 实际项目中需要调用电话服务商的API
            // 例如：阿里云语音、腾讯云语音等
            
        } catch (Exception e) {
            log.error("拨打电话失败: {}", phoneNumber, e);
            throw new RuntimeException("拨打电话失败", e);
        }
    }
} 