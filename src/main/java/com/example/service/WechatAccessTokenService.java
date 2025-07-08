package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WechatAccessTokenService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.appsecret}")
    private String appSecret;

    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={appsecret}";
    private static final String ACCESS_TOKEN_KEY = "wechat:access_token";
    private static final long ACCESS_TOKEN_EXPIRE = 7000; // 微信官方说2小时，我们提前100秒过期

    /**
     * 获取Access Token
     */
    public String getAccessToken() {
        // 先从Redis获取
        String accessToken = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
        if (accessToken != null && !accessToken.isEmpty()) {
            log.debug("从Redis获取Access Token: {}", accessToken);
            return accessToken;
        }

        // Redis没有，从微信服务器获取
        return refreshAccessToken();
    }

    /**
     * 刷新Access Token
     */
    public String refreshAccessToken() {
        try {
            log.info("开始获取新的Access Token");
            
            String url = ACCESS_TOKEN_URL.replace("{appid}", appId).replace("{appsecret}", appSecret);
            AccessTokenResponse response = restTemplate.getForObject(url, AccessTokenResponse.class);
            
            if (response != null && response.getAccess_token() != null) {
                String accessToken = response.getAccess_token();
                
                // 存储到Redis
                redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, accessToken, ACCESS_TOKEN_EXPIRE, TimeUnit.SECONDS);
                
                log.info("成功获取Access Token: {}", accessToken);
                return accessToken;
            } else {
                log.error("获取Access Token失败: {}", response);
                throw new RuntimeException("获取Access Token失败");
            }
        } catch (Exception e) {
            log.error("获取Access Token异常", e);
            throw new RuntimeException("获取Access Token异常", e);
        }
    }

    /**
     * 清除Access Token缓存
     */
    public void clearAccessToken() {
        redisTemplate.delete(ACCESS_TOKEN_KEY);
        log.info("清除Access Token缓存");
    }

    /**
     * 检查Access Token是否有效
     */
    public boolean isAccessTokenValid(String accessToken) {
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token=" + accessToken;
            String response = restTemplate.getForObject(url, String.class);
            return response != null && !response.contains("errcode");
        } catch (Exception e) {
            log.error("检查Access Token有效性失败", e);
            return false;
        }
    }

    /**
     * Access Token响应类
     */
    public static class AccessTokenResponse {
        private String access_token;
        private Integer expires_in;
        private Integer errcode;
        private String errmsg;

        // Getters and Setters
        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public Integer getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }

        public Integer getErrcode() {
            return errcode;
        }

        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }
    }
} 