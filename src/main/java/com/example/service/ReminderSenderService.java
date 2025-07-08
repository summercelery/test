package com.example.service;

import com.example.entity.Reminder;
import com.example.entity.ReminderRecipient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ReminderSenderService {

    @Autowired
    private WechatMessageService wechatMessageService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private PhoneService phoneService;

    /**
     * 发送提醒
     */
    public void sendReminder(Reminder reminder, ReminderRecipient recipient) {
        log.info("发送提醒: reminderId={}, recipientId={}, types={}", 
                reminder.getId(), recipient.getId(), reminder.getReminderTypes());

        try {
            // 解析提醒类型
            List<String> reminderTypes = reminder.getReminderTypes();
            if (reminderTypes == null || reminderTypes.isEmpty()) {
                throw new IllegalArgumentException("提醒类型不能为空");
            }
            
            boolean sent = false;
            
            for (String typeStr : reminderTypes) {
                try {
                    Reminder.ReminderType type = Reminder.ReminderType.valueOf(typeStr.trim());
                    switch (type) {
                        case WECHAT:
                            sendWechatReminder(reminder, recipient);
                            sent = true;
                            break;
                        case SMS:
                            sendSmsReminder(reminder, recipient);
                            sent = true;
                            break;
                        case PHONE:
                            sendPhoneReminder(reminder, recipient);
                            sent = true;
                            break;
                        default:
                            log.warn("不支持的提醒类型: {}", type);
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("无效的提醒类型: {}", typeStr);
                }
            }
            
            if (!sent) {
                throw new IllegalArgumentException("没有成功发送任何类型的提醒");
            }
            
        } catch (Exception e) {
            log.error("发送提醒失败: reminderId={}, recipientId={}", reminder.getId(), recipient.getId(), e);
            throw e;
        }
    }

    /**
     * 发送微信公众号提醒
     */
    private void sendWechatReminder(Reminder reminder, ReminderRecipient recipient) {
        if (recipient.getWechatOpenid() == null || recipient.getWechatOpenid().isEmpty()) {
            throw new IllegalArgumentException("接收者没有微信OpenID");
        }

        // 这里需要调用微信公众号的客服消息接口
        // 由于需要Access Token，这里先记录日志
        log.info("发送微信公众号提醒: openid={}, title={}, content={}", 
                recipient.getWechatOpenid(), reminder.getTitle(), reminder.getContent());
        
        // TODO: 实现微信公众号消息发送
        // 需要调用微信客服消息接口：https://api.weixin.qq.com/cgi-bin/message/custom/send
    }

    /**
     * 发送短信提醒
     */
    private void sendSmsReminder(Reminder reminder, ReminderRecipient recipient) {
        if (recipient.getPhoneNumber() == null || recipient.getPhoneNumber().isEmpty()) {
            throw new IllegalArgumentException("接收者没有手机号码");
        }

        String message = String.format("【提醒】%s\n%s", reminder.getTitle(), reminder.getContent());
        smsService.sendMessage(recipient.getPhoneNumber(), message);
    }

    /**
     * 发送电话提醒
     */
    private void sendPhoneReminder(Reminder reminder, ReminderRecipient recipient) {
        if (recipient.getPhoneNumber() == null || recipient.getPhoneNumber().isEmpty()) {
            throw new IllegalArgumentException("接收者没有手机号码");
        }

        String message = String.format("提醒：%s。%s", reminder.getTitle(), reminder.getContent());
        phoneService.makeCall(recipient.getPhoneNumber(), message);
    }
} 