package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.dto.SmsLoginRequest;
import com.example.dto.SendSmsRequest;
import com.example.dto.ResetPasswordRequest;
import com.example.dto.WechatLoginRequest;
import com.example.service.AuthService;
import com.example.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RedisService redisService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            AuthResponse authResponse = authService.login(loginRequest, request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = authService.register(registerRequest);
            return ResponseEntity.ok(ApiResponse.success("注册成功", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("认证测试成功", "Hello World!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                redisService.deleteUserSession(token);
            }
            return ResponseEntity.ok(ApiResponse.success("退出登录成功", "已清除用户会话"));
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

    /**
     * 发送短信验证码
     */
    @PostMapping("/send-sms")
    public ResponseEntity<ApiResponse<String>> sendSmsCode(@Valid @RequestBody SendSmsRequest sendSmsRequest) {
        try {
            boolean success = authService.sendSmsCode(sendSmsRequest);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("验证码发送成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("验证码发送失败，请稍后重试"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/sms-login")
    public ResponseEntity<ApiResponse<AuthResponse>> smsLogin(@Valid @RequestBody SmsLoginRequest smsLoginRequest, HttpServletRequest request) {
        try {
            AuthResponse authResponse = authService.smsLogin(smsLoginRequest, request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            boolean success = authService.resetPassword(resetPasswordRequest);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("密码重置失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 微信登录
     */
    @PostMapping("/wechat-login")
    public ResponseEntity<ApiResponse<AuthResponse>> wechatLogin(@Valid @RequestBody WechatLoginRequest wechatLoginRequest, HttpServletRequest request) {
        try {
            AuthResponse authResponse = authService.wechatLogin(wechatLoginRequest, request);
            return ResponseEntity.ok(ApiResponse.success("微信登录成功", authResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取微信授权URL
     */
    @GetMapping("/wechat/auth-url")
    public ResponseEntity<ApiResponse<String>> getWechatAuthUrl(@RequestParam String redirectUri, @RequestParam(required = false) String state) {
        try {
            String authUrl = authService.getWechatAuthUrl(redirectUri, state);
            return ResponseEntity.ok(ApiResponse.success("获取微信授权URL成功", authUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 