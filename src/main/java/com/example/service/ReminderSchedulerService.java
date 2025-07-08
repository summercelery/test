package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReminderSchedulerService {

    @Autowired
    private ReminderService reminderService;

    /**
     * 每分钟检查一次待发送的提醒
     */
    @Scheduled(fixedRate = 60000) // 60秒 = 1分钟
    public void processPendingReminders() {
        try {
            log.debug("开始处理待发送的提醒");
            reminderService.processPendingReminders();
        } catch (Exception e) {
            log.error("处理待发送提醒失败", e);
        }
    }

    /**
     * 每天凌晨2点清理过期的提醒
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredReminders() {
        try {
            log.info("开始清理过期的提醒");
            // TODO: 实现清理过期提醒的逻辑
            // 可以删除已发送且超过一定时间的提醒记录
        } catch (Exception e) {
            log.error("清理过期提醒失败", e);
        }
    }

    /**
     * 每小时检查一次重复提醒
     */
    @Scheduled(fixedRate = 3600000) // 3600000毫秒 = 1小时
    public void processRepeatingReminders() {
        try {
            log.debug("开始处理重复提醒");
            // 重复提醒的处理逻辑已经在processPendingReminders中实现
        } catch (Exception e) {
            log.error("处理重复提醒失败", e);
        }
    }
} 