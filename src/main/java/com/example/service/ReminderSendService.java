package com.example.service;

import com.example.entity.Reminder;
import com.example.entity.ReminderRecipient;
import com.example.repository.ReminderRecipientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提醒发送服务类
 * 负责发送各种类型的提醒（微信、短信、电话）
 */
@Slf4j
@Service
public class ReminderSendService {

    @Autowired
    private ReminderRecipientRepository recipientRepository;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private PhoneService phoneService;

    /**
     * 发送提醒
     */
    public void sendReminder(Reminder reminder) {
        List<ReminderRecipient> recipients = recipientRepository.findByReminderId(reminder.getId());
        
        for (ReminderRecipient recipient : recipients) {
            try {
                sendToRecipient(reminder, recipient);
            } catch (Exception e) {
                log.error("发送提醒给接收者失败: {}", recipient.getRecipientValue(), e);
            }
        }
    }

    /**
     * 发送给单个接收者
     */
    private void sendToRecipient(Reminder reminder, ReminderRecipient recipient) {
        String message = buildMessage(reminder);
        
        switch (recipient.getRecipientType()) {
            case WECHAT:
                sendWechatMessage(recipient.getRecipientValue(), message);
                break;
            case PHONE:
                sendSmsMessage(recipient.getRecipientValue(), message);
                break;
            default:
                log.warn("未知的接收者类型: {}", recipient.getRecipientType());
        }
    }

    /**
     * 构建提醒消息
     */
    private String buildMessage(Reminder reminder) {
        StringBuilder message = new StringBuilder();
        message.append("【提醒】").append(reminder.getTitle());
        
        if (reminder.getContent() != null && !reminder.getContent().isEmpty()) {
            message.append("\n").append(reminder.getContent());
        }
        
        return message.toString();
    }

    /**
     * 发送微信消息
     */
    private void sendWechatMessage(String openid, String message) {
        try {
            wechatService.sendMessage(openid, message);
            log.info("微信消息发送成功: {}", openid);
        } catch (Exception e) {
            log.error("微信消息发送失败: {}", openid, e);
            throw e;
        }
    }

    /**
     * 发送短信消息
     */
    private void sendSmsMessage(String phoneNumber, String message) {
        try {
            smsService.sendMessage(phoneNumber, message);
            log.info("短信发送成功: {}", phoneNumber);
        } catch (Exception e) {
            log.error("短信发送失败: {}", phoneNumber, e);
            throw e;
        }
    }

    /**
     * 发送电话提醒
     */
    private void sendPhoneCall(String phoneNumber, String message) {
        try {
            phoneService.makeCall(phoneNumber, message);
            log.info("电话提醒成功: {}", phoneNumber);
        } catch (Exception e) {
            log.error("电话提醒失败: {}", phoneNumber, e);
            throw e;
        }
    }
} 