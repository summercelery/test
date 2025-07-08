package com.example.dto;

import lombok.Data;

@Data
public class WechatQRCodeResponse {
    private String qrCodeUrl; // 二维码图片URL
    private String scanId; // 扫码ID，用于轮询状态
    private Integer expireSeconds; // 有效期
} 