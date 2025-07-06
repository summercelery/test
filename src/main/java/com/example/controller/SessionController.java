package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.UserSession;
import com.example.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
@CrossOrigin(origins = "*")
public class SessionController {

    @Autowired
    private RedisService redisService;

    /**
     * 获取当前用户会话信息
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<UserSession>> getCurrentSession(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("未找到有效的token"));
            }

            UserSession userSession = redisService.getUserSession(token);
            if (userSession == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("会话已过期或不存在"));
            }

            return ResponseEntity.ok(ApiResponse.success("获取会话信息成功", userSession));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取会话统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionStats(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("未找到有效的token"));
            }

            UserSession userSession = redisService.getUserSession(token);
            if (userSession == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("会话已过期或不存在"));
            }

            long expireTime = redisService.getSessionExpireTime(token);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("loginTime", userSession.getLoginTime());
            stats.put("lastAccessTime", userSession.getLastAccessTime());
            stats.put("expireTimeSeconds", expireTime);
            stats.put("ipAddress", userSession.getIpAddress());
            stats.put("userAgent", userSession.getUserAgent());

            return ResponseEntity.ok(ApiResponse.success("获取会话统计成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 延长会话时间
     */
    @PostMapping("/extend")
    public ResponseEntity<ApiResponse<String>> extendSession(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("未找到有效的token"));
            }

            redisService.extendSessionExpireTime(token);
            return ResponseEntity.ok(ApiResponse.success("会话已延长", "会话时间已成功延长"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 强制下线其他设备（删除其他会话）
     */
    @PostMapping("/logout-others")
    public ResponseEntity<ApiResponse<String>> logoutOtherDevices(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("未找到有效的token"));
            }

            UserSession currentSession = redisService.getUserSession(token);
            if (currentSession == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("会话已过期或不存在"));
            }

            // 删除该用户的其他会话（保留当前会话）
            String currentToken = redisService.getUserToken(currentSession.getUsername());
            if (currentToken != null && !currentToken.equals(token)) {
                redisService.deleteUserSession(currentToken);
            }

            return ResponseEntity.ok(ApiResponse.success("其他设备已下线", "已成功下线其他设备"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新用户会话信息
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<String>> updateSession(HttpServletRequest request, @RequestBody UserSession userSession) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("未找到有效的token"));
            }

            UserSession existingSession = redisService.getUserSession(token);
            if (existingSession == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("会话已过期或不存在"));
            }

            // 更新会话信息
            existingSession.setFullName(userSession.getFullName());
            existingSession.setEmail(userSession.getEmail());
            existingSession.setPhoneNumber(userSession.getPhoneNumber());
            
            redisService.updateUserSession(token, existingSession);

            return ResponseEntity.ok(ApiResponse.success("会话信息已更新", "用户会话信息已成功更新"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
} 