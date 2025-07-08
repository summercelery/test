package com.example.dto;

import lombok.Data;

@Data
public class WechatQRCodeRequest {
    private String scene; // 场景值，用于标识扫码来源
    private Integer expireSeconds = 300; // 二维码有效期，默认5分钟
} 