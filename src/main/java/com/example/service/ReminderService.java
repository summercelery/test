package com.example.service;

import com.example.dto.ReminderRequest;
import com.example.dto.ReminderResponse;
import com.example.dto.ReminderRecipientRequest;
import com.example.entity.Reminder;
import com.example.entity.ReminderRecipient;
import com.example.entity.User;
import com.example.repository.ReminderRepository;
import com.example.repository.ReminderRecipientRepository;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 提醒服务类
 */
@Slf4j
@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ReminderRecipientRepository recipientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReminderSenderService reminderSenderService;

    /**
     * 创建提醒
     */
    @Transactional
    public Reminder createReminder(Long userId, ReminderRequest request) {
        log.info("用户 {} 创建提醒: {}", userId, request.getTitle());

        // 验证提醒时间不能早于当前时间
        if (request.getReminderTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("提醒时间不能早于当前时间");
        }

        // 创建提醒
        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setTitle(request.getTitle());
        reminder.setContent(request.getContent());
        reminder.setReminderTime(request.getReminderTime());
        
        // 设置提醒类型列表
        reminder.setReminderTypes(request.getReminderTypes());
        
        // 设置业务类型
        reminder.setType(request.getType());
        
        reminder.setRepeatType(Reminder.RepeatType.valueOf(request.getRepeatType()));
        reminder.setRepeatEndTime(request.getRepeatEndTime());
        reminder.setStatus(Reminder.ReminderStatus.PENDING);

        Reminder savedReminder = reminderRepository.save(reminder);

        // 创建接收者
        List<ReminderRecipient> recipients = request.getRecipients().stream()
                .map(recipientRequest -> createRecipient(savedReminder.getId(), recipientRequest))
                .collect(Collectors.toList());

        recipientRepository.saveAll(recipients);

        return savedReminder;
    }

    /**
     * 获取用户的提醒列表
     */
    public Page<Reminder> getUserReminders(Long userId, Pageable pageable) {
        return reminderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取用户的提醒列表（按状态筛选）
     */
    public Page<Reminder> getUserRemindersByStatus(Long userId, Reminder.ReminderStatus status, Pageable pageable) {
        return reminderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
    }

    /**
     * 搜索用户的提醒
     */
    public Page<Reminder> searchUserReminders(Long userId, String searchQuery, Pageable pageable) {
        return reminderRepository.findByUserIdAndTitleContainingOrContentContainingOrderByCreatedAtDesc(
                userId, searchQuery, searchQuery, pageable);
    }

    /**
     * 获取提醒详情
     */
    public Reminder getReminderById(Long reminderId, Long userId) {
        return reminderRepository.findById(reminderId)
                .filter(reminder -> reminder.getUserId().equals(userId))
                .orElse(null);
    }

    /**
     * 更新提醒
     */
    @Transactional
    public Reminder updateReminder(Long userId, Long reminderId, ReminderRequest request) {
        log.info("用户 {} 更新提醒: {}", userId, reminderId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("提醒不存在"));

        if (!reminder.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此提醒");
        }

        if (reminder.getStatus() != Reminder.ReminderStatus.PENDING) {
            throw new IllegalArgumentException("只能修改待发送的提醒");
        }

        // 更新提醒信息
        reminder.setTitle(request.getTitle());
        reminder.setContent(request.getContent());
        reminder.setReminderTime(request.getReminderTime());
        
        // 设置提醒类型列表
        reminder.setReminderTypes(request.getReminderTypes());
        
        // 设置业务类型
        reminder.setType(request.getType());
        
        reminder.setRepeatType(Reminder.RepeatType.valueOf(request.getRepeatType()));
        reminder.setRepeatEndTime(request.getRepeatEndTime());

        reminder = reminderRepository.save(reminder);

        // 更新接收者
        recipientRepository.deleteByReminderId(reminderId);
        List<ReminderRecipient> recipients = request.getRecipients().stream()
                .map(recipientRequest -> createRecipient(reminderId, recipientRequest))
                .collect(Collectors.toList());
        recipientRepository.saveAll(recipients);

        return reminder;
    }

    /**
     * 删除提醒
     */
    @Transactional
    public boolean deleteReminder(Long userId, Long reminderId) {
        log.info("用户 {} 删除提醒: {}", userId, reminderId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElse(null);

        if (reminder == null) {
            return false;
        }

        if (!reminder.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此提醒");
        }

        // 删除接收者
        recipientRepository.deleteByReminderId(reminderId);
        // 删除提醒
        reminderRepository.delete(reminder);
        return true;
    }

    /**
     * 取消提醒
     */
    @Transactional
    public boolean cancelReminder(Long userId, Long reminderId) {
        log.info("用户 {} 取消提醒: {}", userId, reminderId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElse(null);

        if (reminder == null) {
            return false;
        }

        if (!reminder.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权取消此提醒");
        }

        if (reminder.getStatus() != Reminder.ReminderStatus.PENDING) {
            throw new IllegalArgumentException("只能取消待发送的提醒");
        }

        reminder.setStatus(Reminder.ReminderStatus.CANCELLED);
        reminderRepository.save(reminder);
        return true;
    }

    /**
     * 获取用户提醒统计
     */
    public ReminderStats getReminderStats(Long userId) {
        long total = reminderRepository.countByUserId(userId);
        long pending = reminderRepository.countByUserIdAndStatus(userId, Reminder.ReminderStatus.PENDING);
        long sent = reminderRepository.countByUserIdAndStatus(userId, Reminder.ReminderStatus.SENT);
        long cancelled = reminderRepository.countByUserIdAndStatus(userId, Reminder.ReminderStatus.CANCELLED);

        return new ReminderStats(total, pending, sent, cancelled);
    }

    /**
     * 处理待发送的提醒
     */
    @Transactional
    public void processPendingReminders() {
        log.info("开始处理待发送的提醒");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 处理一次性提醒
        List<Reminder> pendingReminders = reminderRepository.findPendingReminders(now);
        for (Reminder reminder : pendingReminders) {
            if (reminder.getRepeatType() == Reminder.RepeatType.NONE) {
                processReminder(reminder);
            }
        }

        // 处理重复提醒
        List<Reminder> repeatingReminders = reminderRepository.findRepeatingReminders(now);
        for (Reminder reminder : repeatingReminders) {
            processRepeatingReminder(reminder);
        }
    }

    /**
     * 处理单个提醒
     */
    private void processReminder(Reminder reminder) {
        try {
            List<ReminderRecipient> recipients = recipientRepository.findByReminderIdAndStatus(
                    reminder.getId(), ReminderRecipient.Status.ACTIVE);

            boolean allSent = true;
            for (ReminderRecipient recipient : recipients) {
                try {
                    reminderSenderService.sendReminder(reminder, recipient);
                    recipient.setLastSentTime(LocalDateTime.now());
                    recipient.setSentCount(recipient.getSentCount() + 1);
                    recipientRepository.save(recipient);
                } catch (Exception e) {
                    log.error("发送提醒失败: reminderId={}, recipientId={}", reminder.getId(), recipient.getId(), e);
                    allSent = false;
                }
            }

            reminder.setLastSentTime(LocalDateTime.now());
            reminder.setSentCount(reminder.getSentCount() + 1);
            
            if (allSent) {
                reminder.setStatus(Reminder.ReminderStatus.SENT);
            } else {
                reminder.setStatus(Reminder.ReminderStatus.FAILED);
            }
            
            reminderRepository.save(reminder);
            
        } catch (Exception e) {
            log.error("处理提醒失败: reminderId={}", reminder.getId(), e);
            reminder.setStatus(Reminder.ReminderStatus.FAILED);
            reminderRepository.save(reminder);
        }
    }

    /**
     * 处理重复提醒
     */
    private void processRepeatingReminder(Reminder reminder) {
        try {
            List<ReminderRecipient> recipients = recipientRepository.findByReminderIdAndStatus(
                    reminder.getId(), ReminderRecipient.Status.ACTIVE);

            boolean allSent = true;
            for (ReminderRecipient recipient : recipients) {
                try {
                    reminderSenderService.sendReminder(reminder, recipient);
                    recipient.setLastSentTime(LocalDateTime.now());
                    recipient.setSentCount(recipient.getSentCount() + 1);
                    recipientRepository.save(recipient);
                } catch (Exception e) {
                    log.error("发送重复提醒失败: reminderId={}, recipientId={}", reminder.getId(), recipient.getId(), e);
                    allSent = false;
                }
            }

            reminder.setLastSentTime(LocalDateTime.now());
            reminder.setSentCount(reminder.getSentCount() + 1);
            
            // 检查是否需要结束重复
            if (reminder.getRepeatEndTime() != null && LocalDateTime.now().isAfter(reminder.getRepeatEndTime())) {
                reminder.setStatus(Reminder.ReminderStatus.SENT);
            }
            
            reminderRepository.save(reminder);
            
        } catch (Exception e) {
            log.error("处理重复提醒失败: reminderId={}", reminder.getId(), e);
        }
    }

    /**
     * 创建接收者
     */
    private ReminderRecipient createRecipient(Long reminderId, ReminderRecipientRequest request) {
        ReminderRecipient recipient = new ReminderRecipient();
        recipient.setReminderId(reminderId);
        recipient.setRecipientType(ReminderRecipient.RecipientType.valueOf(request.getRecipientType()));
        recipient.setRecipientValue(request.getRecipientValue());
        
        // 根据接收者类型设置相应的字段
        if ("PHONE".equals(request.getRecipientType())) {
            recipient.setPhoneNumber(request.getRecipientValue());
        } else if ("WECHAT".equals(request.getRecipientType())) {
            recipient.setWechatOpenid(request.getRecipientValue());
        }
        
        recipient.setStatus(ReminderRecipient.Status.ACTIVE);
        return recipient;
    }

    /**
     * 转换为响应对象
     */
    private ReminderResponse convertToResponse(Reminder reminder) {
        List<ReminderRecipient> recipients = recipientRepository.findByReminderId(reminder.getId());
        return convertToResponse(reminder, recipients);
    }

    /**
     * 转换为响应对象
     */
    private ReminderResponse convertToResponse(Reminder reminder, List<ReminderRecipient> recipients) {
        ReminderResponse response = new ReminderResponse();
        response.setId(reminder.getId());
        response.setTitle(reminder.getTitle());
        response.setContent(reminder.getContent());
        response.setReminderTime(reminder.getReminderTime());
        
        // 处理多选提醒类型
        List<String> reminderTypes = reminder.getReminderTypes();
        if (reminderTypes != null && !reminderTypes.isEmpty()) {
            String typesDescription = reminderTypes.stream()
                    .map(type -> Reminder.ReminderType.valueOf(type).getDescription())
                    .collect(Collectors.joining("、"));
            response.setReminderType(typesDescription);
        } else {
            response.setReminderType("未设置");
        }
        
        // 设置业务类型
        response.setType(reminder.getType());
        
        response.setStatus(reminder.getStatus().getDescription());
        response.setRepeatType(reminder.getRepeatType().getDescription());
        response.setRepeatInterval(reminder.getRepeatInterval());
        response.setRepeatEndTime(reminder.getRepeatEndTime());
        response.setCreatedAt(reminder.getCreatedAt());
        response.setLastSentTime(reminder.getLastSentTime());
        response.setSentCount(reminder.getSentCount());

        List<ReminderResponse.RecipientResponse> recipientResponses = recipients.stream()
                .map(this::convertToRecipientResponse)
                .collect(Collectors.toList());
        response.setRecipients(recipientResponses);

        return response;
    }

    /**
     * 转换为接收者响应对象
     */
    private ReminderResponse.RecipientResponse convertToRecipientResponse(ReminderRecipient recipient) {
        ReminderResponse.RecipientResponse response = new ReminderResponse.RecipientResponse();
        response.setId(recipient.getId());
        response.setName(recipient.getName());
        response.setPhoneNumber(recipient.getPhoneNumber());
        response.setWechatOpenid(recipient.getWechatOpenid());
        response.setEmail(recipient.getEmail());
        response.setRelationship(recipient.getRelationship());
        response.setIsRegisteredUser(recipient.getIsRegisteredUser());
        response.setUserId(recipient.getUserId());
        response.setLastSentTime(recipient.getLastSentTime());
        response.setSentCount(recipient.getSentCount());
        response.setStatus(recipient.getStatus().getDescription());
        return response;
    }

    /**
     * 提醒统计类
     */
    public static class ReminderStats {
        private final long total;
        private final long pending;
        private final long sent;
        private final long cancelled;

        public ReminderStats(long total, long pending, long sent, long cancelled) {
            this.total = total;
            this.pending = pending;
            this.sent = sent;
            this.cancelled = cancelled;
        }

        // Getters
        public long getTotal() { return total; }
        public long getPending() { return pending; }
        public long getSent() { return sent; }
        public long getCancelled() { return cancelled; }
    }
} 