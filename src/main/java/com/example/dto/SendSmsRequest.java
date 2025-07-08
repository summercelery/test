package com.example.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SendSmsRequest {
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;
    
    @NotBlank(message = "短信类型不能为空")
    private String smsType; // LOGIN, RESET_PASSWORD
    
    // 图形验证码相关字段
    private String captchaId;
    private String captchaCode;
    
    // getter和setter方法由@Data注解自动生成
    public String getCaptchaId() {
        return captchaId;
    }
    
    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }
    
    public String getCaptchaCode() {
        return captchaCode;
    }
    
    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
} 