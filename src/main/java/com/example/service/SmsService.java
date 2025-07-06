package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    @Autowired
    private RedisService redisService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<String, Integer> sendCountMap = new ConcurrentHashMap<>();
    
    // 验证码有效期（分钟）
    private static final int CODE_EXPIRE_MINUTES = 5;
    // 发送间隔（秒）
    private static final int SEND_INTERVAL_SECONDS = 60;
    // 每日发送限制
    private static final int DAILY_SEND_LIMIT = 10;

    /**
     * 发送验证码
     */
    public boolean sendVerificationCode(String phoneNumber, String smsType) {
        // 检查发送频率限制
        if (!checkSendLimit(phoneNumber)) {
            return false;
        }

        // 生成6位验证码
        String verificationCode = generateVerificationCode();
        
        // 存储到Redis，设置过期时间
        String redisKey = getRedisKey(phoneNumber, smsType);
        redisService.setValue(redisKey, verificationCode, CODE_EXPIRE_MINUTES * 60);
        
        // 更新发送计数
        updateSendCount(phoneNumber);
        
        // 模拟发送短信（实际项目中需要集成短信服务商API）
        boolean sendResult = simulateSendSms(phoneNumber, verificationCode, smsType);
        
        if (sendResult) {
            // 设置发送间隔限制
            setSendIntervalLimit(phoneNumber);
            return true;
        }
        
        return false;
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
     * 模拟发送短信
     */
    private boolean simulateSendSms(String phoneNumber, String code, String smsType) {
        try {
            // 模拟网络延迟
            Thread.sleep(100);
            
            String message;
            switch (smsType) {
                case "LOGIN":
                    message = String.format("【用户认证系统】您的登录验证码是：%s，%d分钟内有效，请勿泄露给他人。", code, CODE_EXPIRE_MINUTES);
                    break;
                case "RESET_PASSWORD":
                    message = String.format("【用户认证系统】您的密码重置验证码是：%s，%d分钟内有效，请勿泄露给他人。", code, CODE_EXPIRE_MINUTES);
                    break;
                default:
                    message = String.format("【用户认证系统】您的验证码是：%s，%d分钟内有效，请勿泄露给他人。", code, CODE_EXPIRE_MINUTES);
            }
            
            // 实际项目中这里需要调用短信服务商API
            System.out.println("发送短信到 " + phoneNumber + ": " + message);
            
            return true;
        } catch (Exception e) {
            System.err.println("发送短信失败: " + e.getMessage());
            return false;
        }
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