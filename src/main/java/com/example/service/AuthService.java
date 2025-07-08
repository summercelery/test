package com.example.service;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.dto.SmsLoginRequest;
import com.example.dto.SendSmsRequest;
import com.example.dto.ResetPasswordRequest;
import com.example.dto.WechatLoginRequest;
import com.example.dto.WechatUserInfo;
import com.example.dto.UserSession;
import com.example.entity.User;
import com.example.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.persistence.Column;
import org.hibernate.annotations.Type;
import java.util.List;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private WechatService wechatService;

    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(userDetails);
        
        User user = userService.findByUsername(userDetails.getUsername());
        
        // 创建用户会话信息
        UserSession userSession = new UserSession(
                user.getId().toString(),
                user.getUsername(),
                user.getRole().name(),
                token
        );
        
        // 设置额外信息
        userSession.setPhoneNumber(user.getPhoneNumber());
        userSession.setEmail(user.getEmail());
        userSession.setFullName(user.getFullName());
        userSession.setIpAddress(getClientIpAddress(request));
        userSession.setUserAgent(request.getHeader("User-Agent"));
        
        // 存储到Redis
        redisService.storeUserSession(token, userSession);
        
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        User user = userService.registerUser(registerRequest);
        
        UserDetails userDetails = user;
        String token = jwtTokenUtil.generateToken(userDetails);
        
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    /**
     * 发送短信验证码
     */
    public void sendSmsCode(SendSmsRequest sendSmsRequest) {
        smsService.sendVerificationCode(sendSmsRequest.getPhoneNumber(), sendSmsRequest.getSmsType());
    }

    /**
     * 短信验证码登录
     */
    public AuthResponse smsLogin(SmsLoginRequest smsLoginRequest, HttpServletRequest request) {
        // 验证短信验证码
        if (!smsService.verifyCode(smsLoginRequest.getPhoneNumber(), smsLoginRequest.getVerificationCode(), "LOGIN")) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 根据手机号查找用户
        User user = userService.findByPhoneNumber(smsLoginRequest.getPhoneNumber());
        if (user == null) {
            throw new RuntimeException("用户不存在，请先注册");
        }

        // 生成token
        UserDetails userDetails = user;
        String token = jwtTokenUtil.generateToken(userDetails);
        
        // 创建用户会话信息
        UserSession userSession = new UserSession(
                user.getId().toString(),
                user.getUsername(),
                user.getRole().name(),
                token
        );
        
        // 设置额外信息
        userSession.setPhoneNumber(user.getPhoneNumber());
        userSession.setEmail(user.getEmail());
        userSession.setFullName(user.getFullName());
        userSession.setIpAddress(getClientIpAddress(request));
        userSession.setUserAgent(request.getHeader("User-Agent"));
        
        // 存储到Redis
        redisService.storeUserSession(token, userSession);
        
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    /**
     * 重置密码
     */
    public boolean resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // 验证短信验证码
        if (!smsService.verifyCode(resetPasswordRequest.getPhoneNumber(), resetPasswordRequest.getVerificationCode(), "RESET_PASSWORD")) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 验证新密码
        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 根据手机号查找用户
        User user = userService.findByPhoneNumber(resetPasswordRequest.getPhoneNumber());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新密码
        return userService.updatePassword(user.getId(), resetPasswordRequest.getNewPassword());
    }

    /**
     * 微信登录
     */
    public AuthResponse wechatLogin(WechatLoginRequest wechatLoginRequest, HttpServletRequest request) {
        // 通过授权码获取微信用户信息
        WechatUserInfo wechatUserInfo = wechatService.getUserInfoByCode(wechatLoginRequest.getCode());
        
        // 查找是否已有绑定该微信的用户
        User user = userService.findByWechatOpenid(wechatUserInfo.getOpenid());
        
        if (user == null) {
            // 如果没有找到，尝试通过unionid查找
            if (wechatUserInfo.getUnionid() != null) {
                user = userService.findByWechatUnionid(wechatUserInfo.getUnionid());
            }
        }
        
        if (user == null) {
            // 如果都没有找到，创建新用户
            user = userService.createWechatUser(wechatUserInfo);
        }
        
        // 生成token
        UserDetails userDetails = user;
        String token = jwtTokenUtil.generateToken(userDetails);
        
        // 创建用户会话信息
        UserSession userSession = new UserSession(
                user.getId().toString(),
                user.getUsername(),
                user.getRole().name(),
                token
        );
        
        // 设置额外信息
        userSession.setPhoneNumber(user.getPhoneNumber());
        userSession.setEmail(user.getEmail());
        userSession.setFullName(user.getFullName());
        userSession.setIpAddress(getClientIpAddress(request));
        userSession.setUserAgent(request.getHeader("User-Agent"));
        
        // 存储到Redis
        redisService.storeUserSession(token, userSession);
        
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    /**
     * 获取微信授权URL
     */
    public String getWechatAuthUrl(String redirectUri, String state) {
        if (state == null || state.isEmpty()) {
            state = "wechat_login_" + System.currentTimeMillis();
        }
        return wechatService.generateAuthUrl(redirectUri, state);
    }
} 