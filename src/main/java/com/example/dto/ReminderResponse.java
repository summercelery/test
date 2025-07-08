package com.example.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReminderResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime reminderTime;
    private String reminderType;
    private String status;
    private String repeatType;
    private Integer repeatInterval;
    private LocalDateTime repeatEndTime;
    private LocalDateTime createdAt;
    private LocalDateTime lastSentTime;
    private Integer sentCount;
    private List<RecipientResponse> recipients;

    @Data
    public static class RecipientResponse {
        private Long id;
        private String name;
        private String phoneNumber;
        private String wechatOpenid;
        private String email;
        private String relationship;
        private Boolean isRegisteredUser;
        private Long userId;
        private LocalDateTime lastSentTime;
        private Integer sentCount;
        private String status;
    }
} 