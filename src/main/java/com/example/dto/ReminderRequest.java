package com.example.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒请求DTO
 */
@Data
public class ReminderRequest {

    @NotBlank(message = "提醒标题不能为空")
    @Size(max = 200, message = "提醒标题长度不能超过200个字符")
    private String title;

    @Size(max = 1000, message = "提醒内容长度不能超过1000个字符")
    private String content;

    @NotNull(message = "提醒时间不能为空")
    private LocalDateTime reminderTime;

    @NotNull(message = "提醒类型不能为空")
    @Size(min = 1, message = "至少选择一种提醒类型")
    private List<String> reminderTypes; // WECHAT, SMS, PHONE

    private String repeatType = "NONE"; // NONE, DAILY, WEEKLY, MONTHLY

    private LocalDateTime repeatEndTime;

    @NotNull(message = "接收者不能为空")
    @Size(min = 1, message = "至少添加一个接收者")
    private List<ReminderRecipientRequest> recipients;
} 