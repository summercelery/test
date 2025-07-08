package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务类
 */
@Slf4j
@Service
public class SmsService {

    @Autowired
    private RedisService redisService;

    @Value("${sms.api-key}")
    private String apiKey;

    @Value("${sms.api-secret}")
    private String apiSecret;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<String, Integer> sendCountMap = new ConcurrentHashMap<>();
    
    // 验证码有效期（分钟）
    private static final int CODE_EXPIRE_MINUTES = 5;
    // 发送间隔（秒）
    private static final int SEND_INTERVAL_SECONDS = 60;
    // 每日发送限制
    private static final int DAILY_SEND_LIMIT = 10;

    /**
     * 发送短信
     */
    public void sendMessage(String phoneNumber, String message) {
        try {
            // TODO: 集成实际的短信服务商API
            log.info("发送短信到 {}: {}", phoneNumber, message);
            
            // 模拟发送成功
            // 实际项目中需要调用短信服务商的API
            // 例如：阿里云短信、腾讯云短信等
            
        } catch (Exception e) {
            log.error("发送短信失败: {}", phoneNumber, e);
            throw new RuntimeException("发送短信失败", e);
        }
    }

    /**
     * 发送验证码
     */
    public void sendVerificationCode(String phoneNumber, String code) {
        String message = String.format("您的验证码是：%s，有效期5分钟，请勿泄露给他人。", code);
        sendMessage(phoneNumber, message);
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String phoneNumber, String verificationCode, String smsType) {
        String redisKey = getRedisKey(phoneNumber, smsType);
        String storedCode = redisService.getValue(redisKey);
        
        if (storedCode != null && storedCode.equals(verificationCode)) {
            // 验证成功后删除验证码
            redisService.deleteKey(redisKey);
            return true;
        }
        
        return false;
    }

    /**
     * 生成6位验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 获取Redis键
     */
    private String getRedisKey(String phoneNumber, String smsType) {
        return String.format("sms:code:%s:%s", smsType, phoneNumber);
    }

    /**
     * 检查发送限制
     */
    private boolean checkSendLimit(String phoneNumber) {
        // 检查发送间隔
        String intervalKey = "sms:interval:" + phoneNumber;
        if (redisService.hasKey(intervalKey)) {
            return false;
        }
        
        // 检查每日发送次数
        String countKey = "sms:count:" + phoneNumber + ":" + getTodayDate();
        String countStr = redisService.getValue(countKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        
        return count < DAILY_SEND_LIMIT;
    }

    /**
     * 更新发送计数
     */
    private void updateSendCount(String phoneNumber) {
        String countKey = "sms:count:" + phoneNumber + ":" + getTodayDate();
        String countStr = redisService.getValue(countKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        
        redisService.setValue(countKey, String.valueOf(count + 1), 24 * 60 * 60); // 24小时过期
    }

    /**
     * 设置发送间隔限制
     */
    private void setSendIntervalLimit(String phoneNumber) {
        String intervalKey = "sms:interval:" + phoneNumber;
        redisService.setValue(intervalKey, "1", SEND_INTERVAL_SECONDS);
    }

    /**
     * 获取今天日期字符串
     */
    private String getTodayDate() {
        return java.time.LocalDate.now().toString();
    }

    /**
     * 获取剩余发送次数
     */
    public int getRemainingSendCount(String phoneNumber) {
        String countKey = "sms:count:" + phoneNumber + ":" + getTodayDate();
        String countStr = redisService.getValue(countKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        return Math.max(0, DAILY_SEND_LIMIT - count);
    }

    /**
     * 获取下次可发送时间（秒）
     */
    public long getNextSendTime(String phoneNumber) {
        String intervalKey = "sms:interval:" + phoneNumber;
        return redisService.getExpire(intervalKey);
    }
} 