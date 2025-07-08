package com.example.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 提醒接收者请求DTO
 */
@Data
public class ReminderRecipientRequest {

    @NotNull(message = "接收者类型不能为空")
    private String recipientType; // PHONE, WECHAT

    @NotBlank(message = "接收者值不能为空")
    private String recipientValue; // 手机号或微信OpenID
} 