package com.example.dto;

import lombok.Data;

@Data
public class WechatUserInfo {
    
    private String openid;
    private String nickname;
    private String sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    private String unionid;
    private String[] privilege;
} 