package com.example.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WechatLoginRequest {
    
    @NotBlank(message = "微信授权码不能为空")
    private String code;
    
    private String state;
} 