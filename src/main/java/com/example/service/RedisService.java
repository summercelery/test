package com.example.service;

import com.example.dto.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final String USER_TOKEN_PREFIX = "user:token:";
    private static final long SESSION_EXPIRE_TIME = 24 * 60 * 60; // 24小时

    /**
     * 存储用户会话信息
     */
    public void storeUserSession(String token, UserSession userSession) {
        String sessionKey = USER_SESSION_PREFIX + token;
        String tokenKey = USER_TOKEN_PREFIX + userSession.getUsername();
        
        // 存储会话信息
        redisTemplate.opsForValue().set(sessionKey, userSession, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
        
        // 存储用户token映射
        redisTemplate.opsForValue().set(tokenKey, token, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 根据token获取用户会话信息
     */
    public UserSession getUserSession(String token) {
        String sessionKey = USER_SESSION_PREFIX + token;
        Object result = redisTemplate.opsForValue().get(sessionKey);
        
        if (result instanceof UserSession) {
            UserSession userSession = (UserSession) result;
            // 更新最后访问时间
            userSession.updateLastAccessTime();
            // 重新设置过期时间
            redisTemplate.expire(sessionKey, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
            return userSession;
        }
        
        return null;
    }

    /**
     * 根据用户名获取token
     */
    public String getUserToken(String username) {
        String tokenKey = USER_TOKEN_PREFIX + username;
        Object result = redisTemplate.opsForValue().get(tokenKey);
        return result != null ? result.toString() : null;
    }

    /**
     * 删除用户会话
     */
    public void deleteUserSession(String token) {
        String sessionKey = USER_SESSION_PREFIX + token;
        Object session = redisTemplate.opsForValue().get(sessionKey);
        
        if (session instanceof UserSession) {
            UserSession userSession = (UserSession) session;
            String tokenKey = USER_TOKEN_PREFIX + userSession.getUsername();
            
            // 删除会话信息
            redisTemplate.delete(sessionKey);
            // 删除token映射
            redisTemplate.delete(tokenKey);
        }
    }

    /**
     * 删除用户会话（根据用户名）
     */
    public void deleteUserSessionByUsername(String username) {
        String tokenKey = USER_TOKEN_PREFIX + username;
        Object token = redisTemplate.opsForValue().get(tokenKey);
        
        if (token != null) {
            String sessionKey = USER_SESSION_PREFIX + token;
            redisTemplate.delete(sessionKey);
            redisTemplate.delete(tokenKey);
        }
    }

    /**
     * 检查token是否存在
     */
    public boolean isTokenExists(String token) {
        String sessionKey = USER_SESSION_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
    }

    /**
     * 更新用户会话信息
     */
    public void updateUserSession(String token, UserSession userSession) {
        String sessionKey = USER_SESSION_PREFIX + token;
        redisTemplate.opsForValue().set(sessionKey, userSession, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 获取会话剩余过期时间
     */
    public long getSessionExpireTime(String token) {
        String sessionKey = USER_SESSION_PREFIX + token;
        Long expireTime = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
        return expireTime != null ? expireTime : 0;
    }

    /**
     * 延长会话过期时间
     */
    public void extendSessionExpireTime(String token) {
        String sessionKey = USER_SESSION_PREFIX + token;
        redisTemplate.expire(sessionKey, SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    // ==================== 短信验证码相关方法 ====================

    /**
     * 设置键值对（带过期时间）
     */
    public void setValue(String key, String value, long expireSeconds) {
        redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 获取值
     */
    public String getValue(String key) {
        Object result = redisTemplate.opsForValue().get(key);
        return result != null ? result.toString() : null;
    }

    /**
     * 删除键
     */
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 检查键是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取键的剩余过期时间（秒）
     */
    public long getExpire(String key) {
        Long expireTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expireTime != null ? expireTime : 0;
    }
} 