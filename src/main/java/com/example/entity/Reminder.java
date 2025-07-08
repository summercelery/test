package com.example.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "reminders")
@EntityListeners(AuditingEntityListener.class)
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 设置提醒的用户ID

    @Column(name = "title", nullable = false, length = 200)
    private String title; // 提醒标题

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 提醒内容

    @Column(name = "reminder_time", nullable = false)
    private LocalDateTime reminderTime; // 提醒时间

    @Column(name = "reminder_types", columnDefinition = "JSON")
    @Type(type = "json")
    private List<String> reminderTypes; // WECHAT, SMS, PHONE

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReminderStatus status = ReminderStatus.PENDING; // 提醒状态

    @Column(name = "repeat_type")
    @Enumerated(EnumType.STRING)
    private RepeatType repeatType = RepeatType.NONE; // 重复类型

    @Column(name = "repeat_interval")
    private Integer repeatInterval = 1; // 重复间隔

    @Column(name = "repeat_end_time")
    private LocalDateTime repeatEndTime; // 重复结束时间

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "last_sent_time")
    private LocalDateTime lastSentTime; // 最后发送时间

    @Column(name = "sent_count")
    private Integer sentCount = 0; // 已发送次数

    @OneToMany(mappedBy = "reminder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReminderRecipient> recipients;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 提醒类型枚举
    public enum ReminderType {
        WECHAT("微信公众号"),
        SMS("短信"),
        PHONE("电话");

        private final String description;

        ReminderType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 提醒状态枚举
    public enum ReminderStatus {
        PENDING("待发送"),
        SENT("已发送"),
        FAILED("发送失败"),
        CANCELLED("已取消");

        private final String description;

        ReminderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 重复类型枚举
    public enum RepeatType {
        NONE("不重复"),
        DAILY("每天"),
        WEEKLY("每周"),
        MONTHLY("每月"),
        YEARLY("每年"),
        CUSTOM("自定义");

        private final String description;

        RepeatType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 