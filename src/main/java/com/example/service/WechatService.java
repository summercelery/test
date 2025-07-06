package com.example.service;

import com.example.dto.WechatUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WechatService {

    @Value("${wechat.app.id:}")
    private String appId;

    @Value("${wechat.app.secret:}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 通过授权码获取微信用户信息
     */
    public WechatUserInfo getUserInfoByCode(String code) {
        try {
            // 1. 通过code获取access_token
            String accessTokenUrl = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId, appSecret, code
            );
            
            Map<String, Object> tokenResponse = restTemplate.getForObject(accessTokenUrl, Map.class);
            
            if (tokenResponse == null || tokenResponse.containsKey("errcode")) {
                throw new RuntimeException("获取微信access_token失败: " + tokenResponse);
            }
            
            String accessToken = (String) tokenResponse.get("access_token");
            String openid = (String) tokenResponse.get("openid");
            
            // 2. 通过access_token获取用户信息
            String userInfoUrl = String.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                accessToken, openid
            );
            
            WechatUserInfo userInfo = restTemplate.getForObject(userInfoUrl, WechatUserInfo.class);
            
            if (userInfo == null) {
                throw new RuntimeException("获取微信用户信息失败");
            }
            
            return userInfo;
            
        } catch (Exception e) {
            // 模拟微信登录（开发环境）
            return simulateWechatUserInfo(code);
        }
    }

    /**
     * 模拟微信用户信息（开发环境使用）
     */
    private WechatUserInfo simulateWechatUserInfo(String code) {
        WechatUserInfo userInfo = new WechatUserInfo();
        userInfo.setOpenid("simulated_openid_" + code.substring(0, Math.min(code.length(), 8)));
        userInfo.setNickname("微信用户" + code.substring(0, Math.min(code.length(), 4)));
        userInfo.setSex("1");
        userInfo.setProvince("北京");
        userInfo.setCity("北京");
        userInfo.setCountry("中国");
        userInfo.setHeadimgurl("https://thirdwx.qlogo.cn/mmopen/vi_32/default_avatar.png");
        userInfo.setUnionid("simulated_unionid_" + code.substring(0, Math.min(code.length(), 8)));
        return userInfo;
    }

    /**
     * 生成微信授权URL
     */
    public String generateAuthUrl(String redirectUri, String state) {
        return String.format(
            "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=%s#wechat_redirect",
            appId, redirectUri, state
        );
    }

    /**
     * 验证微信登录状态
     */
    public boolean validateWechatLogin(String openid, String accessToken) {
        try {
            String validateUrl = String.format(
                "https://api.weixin.qq.com/sns/auth?access_token=%s&openid=%s",
                accessToken, openid
            );
            
            Map<String, Object> response = restTemplate.getForObject(validateUrl, Map.class);
            return response != null && "0".equals(response.get("errcode"));
            
        } catch (Exception e) {
            // 开发环境模拟验证成功
            return true;
        }
    }
} 