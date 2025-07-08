package com.example.dto;

import lombok.Data;

@Data
public class WechatScanStatusResponse {
    private String scanId; // 扫码ID
    private String status; // 状态：PENDING, SCANNED, EXPIRED
    private String openId; // 用户OpenID（扫码成功后返回）
    private String nickname; // 用户昵称（扫码成功后返回）
    private String headimgurl; // 用户头像（扫码成功后返回）
} 