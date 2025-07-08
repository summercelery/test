package com.example.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 提醒接收者实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "reminder_recipients")
@EntityListeners(AuditingEntityListener.class)
public class ReminderRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reminder_id", nullable = false)
    private Long reminderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;

    @Column(name = "recipient_value", nullable = false, length = 100)
    private String recipientValue;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "wechat_openid", length = 100)
    private String wechatOpenid;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "relationship", length = 50)
    private String relationship;

    @Column(name = "is_registered_user")
    private Boolean isRegisteredUser = false;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(name = "last_sent_time")
    private LocalDateTime lastSentTime;

    @Column(name = "sent_count")
    private Integer sentCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reminder_id", insertable = false, updatable = false)
    private Reminder reminder;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 接收者类型枚举
     */
    public enum RecipientType {
        PHONE("手机号"),
        WECHAT("微信");

        private final String description;

        RecipientType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 接收者状态枚举
     */
    public enum Status {
        ACTIVE("活跃"),
        INACTIVE("非活跃"),
        DELETED("已删除");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 