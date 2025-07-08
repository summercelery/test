package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短信发送监控服务
 */
@Slf4j
@Service
public class SmsMonitorService {

    @Autowired
    private RedisService redisService;

    private static final String BLOCKED_PHONE_PREFIX = "sms:blocked:phone:";
    private static final String BLOCKED_IP_PREFIX = "sms:blocked:ip:";
    private static final String ALERT_LOG_PREFIX = "sms:alert:";

    /**
     * 获取短信发送统计信息
     */
    public Map<String, Object> getSmsStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取今日发送统计
        String today = java.time.LocalDate.now().toString();
        String logKey = "sms:log:" + today;
        
        List<Object> logs = redisService.listRange(logKey, 0, -1);
        
        // 统计总发送次数
        stats.put("totalSendCount", logs.size());
        
        // 统计成功/失败次数
        long successCount = logs.stream()
                .mapToLong(log -> {
                    String[] parts = log.toString().split("\\|");
                    return parts.length > 4 && "true".equals(parts[4]) ? 1 : 0;
                })
                .sum();
        
        stats.put("successCount", successCount);
        stats.put("failureCount", logs.size() - successCount);
        
        // 统计不同类型的短信
        Map<String, Long> typeStats = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> {
                            String[] parts = log.toString().split("\\|");
                            return parts.length > 2 ? parts[2] : "UNKNOWN";
                        },
                        Collectors.counting()
                ));
        stats.put("typeStats", typeStats);
        
        // 统计热门手机号（发送次数最多的前10个）
        Map<String, Long> phoneStats = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> {
                            String[] parts = log.toString().split("\\|");
                            return parts.length > 1 ? parts[1] : "UNKNOWN";
                        },
                        Collectors.counting()
                ));
        
        List<Map.Entry<String, Long>> topPhones = phoneStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        stats.put("topPhones", topPhones);
        
        return stats;
    }

    /**
     * 阻止手机号发送短信
     */
    public void blockPhoneNumber(String phoneNumber, int hours) {
        String key = BLOCKED_PHONE_PREFIX + phoneNumber;
        redisService.setValue(key, "blocked", hours * 3600);
        
        // 记录阻止日志
        recordAlert("PHONE_BLOCKED", phoneNumber, "手机号被阻止 " + hours + " 小时");
        
        log.warn("手机号 {} 被阻止发送短信，时长: {} 小时", phoneNumber, hours);
    }

    /**
     * 阻止IP地址发送短信
     */
    public void blockIpAddress(String ipAddress, int hours) {
        String key = BLOCKED_IP_PREFIX + ipAddress;
        redisService.setValue(key, "blocked", hours * 3600);
        
        // 记录阻止日志
        recordAlert("IP_BLOCKED", ipAddress, "IP地址被阻止 " + hours + " 小时");
        
        log.warn("IP地址 {} 被阻止发送短信，时长: {} 小时", ipAddress, hours);
    }

    /**
     * 检查手机号是否被阻止
     */
    public boolean isPhoneBlocked(String phoneNumber) {
        String key = BLOCKED_PHONE_PREFIX + phoneNumber;
        return redisService.hasKey(key);
    }

    /**
     * 检查IP地址是否被阻止
     */
    public boolean isIpBlocked(String ipAddress) {
        String key = BLOCKED_IP_PREFIX + ipAddress;
        return redisService.hasKey(key);
    }

    /**
     * 解除手机号阻止
     */
    public void unblockPhoneNumber(String phoneNumber) {
        String key = BLOCKED_PHONE_PREFIX + phoneNumber;
        redisService.deleteKey(key);
        
        recordAlert("PHONE_UNBLOCKED", phoneNumber, "手机号解除阻止");
        log.info("手机号 {} 解除阻止", phoneNumber);
    }

    /**
     * 解除IP地址阻止
     */
    public void unblockIpAddress(String ipAddress) {
        String key = BLOCKED_IP_PREFIX + ipAddress;
        redisService.deleteKey(key);
        
        recordAlert("IP_UNBLOCKED", ipAddress, "IP地址解除阻止");
        log.info("IP地址 {} 解除阻止", ipAddress);
    }

    /**
     * 记录警报日志
     */
    private void recordAlert(String type, String target, String message) {
        try {
            String today = java.time.LocalDate.now().toString();
            String alertKey = ALERT_LOG_PREFIX + today;
            
            String alertEntry = String.format("%s|%s|%s|%s", 
                    System.currentTimeMillis(), type, target, message);
            
            redisService.listPush(alertKey, alertEntry);
            redisService.setExpire(alertKey, 30 * 24 * 60 * 60); // 30天过期
        } catch (Exception e) {
            log.error("记录警报日志失败", e);
        }
    }

    /**
     * 获取警报日志
     */
    public List<Map<String, String>> getAlertLogs(int days) {
        List<Map<String, String>> allAlerts = new java.util.ArrayList<>();
        
        for (int i = 0; i < days; i++) {
            java.time.LocalDate date = java.time.LocalDate.now().minusDays(i);
            String alertKey = ALERT_LOG_PREFIX + date.toString();
            
            List<Object> logs = redisService.listRange(alertKey, 0, -1);
            
            for (Object log : logs) {
                String[] parts = log.toString().split("\\|");
                Map<String, String> alert = new HashMap<>();
                if (parts.length >= 4) {
                    alert.put("timestamp", parts[0]);
                    alert.put("type", parts[1]);
                    alert.put("target", parts[2]);
                    alert.put("message", parts[3]);
                    alert.put("time", LocalDateTime.ofEpochSecond(
                            Long.parseLong(parts[0]) / 1000, 0, 
                            java.time.ZoneOffset.systemDefault().getRules().getOffset(java.time.Instant.now())
                    ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                allAlerts.add(alert);
            }
        }
        
        return allAlerts;
    }

    /**
     * 自动检测异常发送行为
     */
    @Scheduled(fixedRate = 300000) // 每5分钟检查一次
    public void autoDetectAbnormalBehavior() {
        try {
            String today = java.time.LocalDate.now().toString();
            String logKey = "sms:log:" + today;
            
            List<Object> logs = redisService.listRange(logKey, 0, -1);
            
            // 统计每个手机号的发送次数
            Map<String, Long> phoneStats = logs.stream()
                    .collect(Collectors.groupingBy(
                            log -> {
                                String[] parts = log.toString().split("\\|");
                                return parts.length > 1 ? parts[1] : "UNKNOWN";
                            },
                            Collectors.counting()
                    ));
            
            // 检查是否有手机号发送过多
            phoneStats.entrySet().stream()
                    .filter(entry -> entry.getValue() > 8) // 超过8次发送
                    .forEach(entry -> {
                        String phoneNumber = entry.getKey();
                        if (!isPhoneBlocked(phoneNumber)) {
                            blockPhoneNumber(phoneNumber, 24); // 阻止24小时
                        }
                    });
            
            // 统计每个IP的发送次数
            Map<String, Long> ipStats = logs.stream()
                    .filter(log -> {
                        String[] parts = log.toString().split("\\|");
                        return parts.length > 3 && !"null".equals(parts[3]);
                    })
                    .collect(Collectors.groupingBy(
                            log -> {
                                String[] parts = log.toString().split("\\|");
                                return parts[3];
                            },
                            Collectors.counting()
                    ));
            
            // 检查是否有IP发送过多
            ipStats.entrySet().stream()
                    .filter(entry -> entry.getValue() > 15) // 超过15次发送
                    .forEach(entry -> {
                        String ipAddress = entry.getKey();
                        if (!isIpBlocked(ipAddress)) {
                            blockIpAddress(ipAddress, 12); // 阻止12小时
                        }
                    });
            
        } catch (Exception e) {
            log.error("自动检测异常发送行为失败", e);
        }
    }

    /**
     * 清理过期的监控数据
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点清理
    public void cleanupExpiredData() {
        log.info("开始清理过期的短信监控数据");
        
        // 清理7天前的发送日志
        try {
            String sevenDaysAgo = java.time.LocalDate.now().minusDays(7).toString();
            String logKey = "sms:log:" + sevenDaysAgo;
            redisService.deleteKey(logKey);
            
            log.info("清理完成: 删除了 {} 的发送日志", sevenDaysAgo);
        } catch (Exception e) {
            log.error("清理过期数据失败", e);
        }
    }
} 