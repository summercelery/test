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

    @Autowired
    private SmsMonitorService smsMonitorService;

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
    // IP每日发送限制
    private static final int IP_DAILY_SEND_LIMIT = 20;
    // IP每小时发送限制
    private static final int IP_HOURLY_SEND_LIMIT = 5;

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
     * 发送验证码（带防护措施）
     */
    public void sendVerificationCode(String phoneNumber, String smsType) {
        sendVerificationCode(phoneNumber, smsType, null);
    }

    /**
     * 发送验证码（带IP地址检查）
     */
    public void sendVerificationCode(String phoneNumber, String smsType, String ipAddress) {
        // 0. 检查监控服务的阻止状态
        if (smsMonitorService.isPhoneBlocked(phoneNumber)) {
            throw new RuntimeException("该手机号已被暂时阻止发送短信，请稍后再试");
        }
        
        if (ipAddress != null && smsMonitorService.isIpBlocked(ipAddress)) {
            throw new RuntimeException("该IP地址已被暂时阻止发送短信，请稍后再试");
        }
        
        // 1. 检查手机号发送限制
        if (!checkPhoneSendLimit(phoneNumber)) {
            long nextSendTime = getNextSendTime(phoneNumber);
            if (nextSendTime > 0) {
                throw new RuntimeException("发送过于频繁，请" + nextSendTime + "秒后再试");
            } else {
                throw new RuntimeException("今日发送次数已达上限，请明天再试");
            }
        }

        // 2. 检查IP发送限制
        if (ipAddress != null && !checkIpSendLimit(ipAddress)) {
            throw new RuntimeException("该IP地址发送过于频繁，请稍后再试");
        }

        // 3. 生成验证码
        String verificationCode = generateVerificationCode();
        
        // 4. 存储验证码到Redis
        String redisKey = getRedisKey(phoneNumber, smsType);
        redisService.setValue(redisKey, verificationCode, CODE_EXPIRE_MINUTES * 60);
        
        // 5. 发送短信
        String message = String.format("您的验证码是：%s，有效期%d分钟，请勿泄露给他人。",
                verificationCode, CODE_EXPIRE_MINUTES);
        sendMessage(phoneNumber, message);
        
        // 6. 更新发送计数
        updateSendCount(phoneNumber);
        setSendIntervalLimit(phoneNumber);
        
        // 7. 更新IP发送计数
        if (ipAddress != null) {
            updateIpSendCount(ipAddress);
        }
        
        // 8. 记录发送日志
        recordSendLog(phoneNumber, smsType, ipAddress, true);
        
        log.info("验证码发送成功 - 手机号: {}, 类型: {}, IP: {}", phoneNumber, smsType, ipAddress);
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
            log.info("验证码验证成功 - 手机号: {}, 类型: {}", phoneNumber, smsType);
            return true;
        }
        
        log.warn("验证码验证失败 - 手机号: {}, 类型: {}, 输入码: {}", phoneNumber, smsType, verificationCode);
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
     * 检查手机号发送限制
     */
    private boolean checkPhoneSendLimit(String phoneNumber) {
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
     * 检查IP发送限制
     */
    private boolean checkIpSendLimit(String ipAddress) {
        // 检查IP每小时发送次数
        String hourlyKey = "sms:ip:hourly:" + ipAddress + ":" + getCurrentHour();
        String hourlyCountStr = redisService.getValue(hourlyKey);
        int hourlyCount = hourlyCountStr != null ? Integer.parseInt(hourlyCountStr) : 0;
        
        if (hourlyCount >= IP_HOURLY_SEND_LIMIT) {
            return false;
        }
        
        // 检查IP每日发送次数
        String dailyKey = "sms:ip:daily:" + ipAddress + ":" + getTodayDate();
        String dailyCountStr = redisService.getValue(dailyKey);
        int dailyCount = dailyCountStr != null ? Integer.parseInt(dailyCountStr) : 0;
        
        return dailyCount < IP_DAILY_SEND_LIMIT;
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
     * 更新IP发送计数
     */
    private void updateIpSendCount(String ipAddress) {
        // 更新每小时计数
        String hourlyKey = "sms:ip:hourly:" + ipAddress + ":" + getCurrentHour();
        String hourlyCountStr = redisService.getValue(hourlyKey);
        int hourlyCount = hourlyCountStr != null ? Integer.parseInt(hourlyCountStr) : 0;
        redisService.setValue(hourlyKey, String.valueOf(hourlyCount + 1), 60 * 60); // 1小时过期
        
        // 更新每日计数
        String dailyKey = "sms:ip:daily:" + ipAddress + ":" + getTodayDate();
        String dailyCountStr = redisService.getValue(dailyKey);
        int dailyCount = dailyCountStr != null ? Integer.parseInt(dailyCountStr) : 0;
        redisService.setValue(dailyKey, String.valueOf(dailyCount + 1), 24 * 60 * 60); // 24小时过期
    }

    /**
     * 设置发送间隔限制
     */
    private void setSendIntervalLimit(String phoneNumber) {
        String intervalKey = "sms:interval:" + phoneNumber;
        redisService.setValue(intervalKey, "1", SEND_INTERVAL_SECONDS);
    }

    /**
     * 记录发送日志
     */
    private void recordSendLog(String phoneNumber, String smsType, String ipAddress, boolean success) {
        try {
            String logKey = "sms:log:" + getTodayDate();
            String logEntry = String.format("%s|%s|%s|%s|%s", 
                    System.currentTimeMillis(), phoneNumber, smsType, ipAddress, success);
            
            // 添加到日志列表
            redisService.listPush(logKey, logEntry);
            // 设置过期时间为7天
            redisService.setExpire(logKey, 7 * 24 * 60 * 60);
        } catch (Exception e) {
            log.error("记录发送日志失败", e);
        }
    }

    /**
     * 获取今天日期字符串
     */
    private String getTodayDate() {
        return java.time.LocalDate.now().toString();
    }

    /**
     * 获取当前小时字符串
     */
    private String getCurrentHour() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
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

    /**
     * 获取IP剩余发送次数
     */
    public int getIpRemainingSendCount(String ipAddress) {
        String dailyKey = "sms:ip:daily:" + ipAddress + ":" + getTodayDate();
        String dailyCountStr = redisService.getValue(dailyKey);
        int dailyCount = dailyCountStr != null ? Integer.parseInt(dailyCountStr) : 0;
        return Math.max(0, IP_DAILY_SEND_LIMIT - dailyCount);
    }

    /**
     * 检查异常发送行为
     */
    public boolean checkAbnormalBehavior(String phoneNumber, String ipAddress) {
        // 检查是否存在异常发送模式
        String countKey = "sms:count:" + phoneNumber + ":" + getTodayDate();
        String countStr = redisService.getValue(countKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        
        // 如果单个手机号发送次数过多，标记为异常
        if (count > DAILY_SEND_LIMIT * 0.8) {
            log.warn("检测到异常发送行为 - 手机号: {}, 今日发送次数: {}", phoneNumber, count);
            return true;
        }
        
        // 检查IP发送行为
        if (ipAddress != null) {
            String ipDailyKey = "sms:ip:daily:" + ipAddress + ":" + getTodayDate();
            String ipDailyCountStr = redisService.getValue(ipDailyKey);
            int ipDailyCount = ipDailyCountStr != null ? Integer.parseInt(ipDailyCountStr) : 0;
            
            if (ipDailyCount > IP_DAILY_SEND_LIMIT * 0.8) {
                log.warn("检测到异常发送行为 - IP: {}, 今日发送次数: {}", ipAddress, ipDailyCount);
                return true;
            }
        }
        
        return false;
    }

} 