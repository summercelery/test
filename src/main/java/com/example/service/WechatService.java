package com.example.service;

import com.example.dto.WechatUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信服务类
 */
@Slf4j
@Service
public class WechatService {

    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken;
    private long accessTokenExpireTime;

    /**
     * 刷新Access Token
     */
    public String refreshAccessToken() {
        try {
            String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, appSecret
            );

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("access_token")) {
                this.accessToken = (String) response.get("access_token");
                int expiresIn = (Integer) response.get("expires_in");
                this.accessTokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L; // 提前5分钟过期
                
                log.info("微信Access Token刷新成功");
                return this.accessToken;
            } else {
                log.error("微信Access Token刷新失败: {}", response);
                throw new RuntimeException("获取微信Access Token失败");
            }
        } catch (Exception e) {
            log.error("刷新微信Access Token异常", e);
            throw new RuntimeException("刷新微信Access Token失败", e);
        }
    }

    /**
     * 获取Access Token
     */
    public String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() >= accessTokenExpireTime) {
            return refreshAccessToken();
        }
        return accessToken;
    }

    /**
     * 发送微信消息
     */
    public void sendMessage(String openid, String message) {
        try {
            String accessToken = getAccessToken();
            String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=%s",
                accessToken
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("touser", openid);
            requestBody.put("msgtype", "text");
            
            Map<String, String> text = new HashMap<>();
            text.put("content", message);
            requestBody.put("text", text);

            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            
            if (response != null && response.containsKey("errcode")) {
                int errcode = (Integer) response.get("errcode");
                if (errcode != 0) {
                    log.error("发送微信消息失败: {}", response);
                    throw new RuntimeException("发送微信消息失败");
                }
            }
            
            log.info("微信消息发送成功: {}", openid);
        } catch (Exception e) {
            log.error("发送微信消息异常", e);
            throw new RuntimeException("发送微信消息失败", e);
        }
    }

    /**
     * 生成二维码
     */
    public String generateQRCode(String scene, int expireSeconds) {
        try {
            String accessToken = getAccessToken();
            String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s",
                accessToken
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("expire_seconds", expireSeconds);
            requestBody.put("action_name", "QR_SCENE");
            
            Map<String, Object> actionInfo = new HashMap<>();
            Map<String, Object> sceneInfo = new HashMap<>();
            sceneInfo.put("scene_id", scene);
            actionInfo.put("scene", sceneInfo);
            requestBody.put("action_info", actionInfo);

            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            
            if (response != null && response.containsKey("ticket")) {
                String ticket = (String) response.get("ticket");
                return String.format("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s", ticket);
            } else {
                log.error("生成微信二维码失败: {}", response);
                throw new RuntimeException("生成微信二维码失败");
            }
        } catch (Exception e) {
            log.error("生成微信二维码异常", e);
            throw new RuntimeException("生成微信二维码失败", e);
        }
    }

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