package com.example.service;

import com.example.dto.RegisterRequest;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(User.Role.USER);

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
    
    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElse(null);
    }

    public boolean updatePassword(Long userId, String newPassword) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public User findByWechatOpenid(String wechatOpenid) {
        return userRepository.findByWechatOpenid(wechatOpenid)
                .orElse(null);
    }

    public User findByWechatUnionid(String wechatUnionid) {
        return userRepository.findByWechatUnionid(wechatUnionid)
                .orElse(null);
    }

    public User createWechatUser(com.example.dto.WechatUserInfo wechatUserInfo) {
        User user = new User();
        user.setUsername("wechat_" + wechatUserInfo.getOpenid().substring(0, Math.min(wechatUserInfo.getOpenid().length(), 8)));
        user.setPassword(passwordEncoder.encode("wechat_" + System.currentTimeMillis())); // 随机密码
        user.setFullName(wechatUserInfo.getNickname());
        user.setWechatOpenid(wechatUserInfo.getOpenid());
        user.setWechatUnionid(wechatUserInfo.getUnionid());
        user.setRole(User.Role.USER);
        
        return userRepository.save(user);
    }

    public User updateUserProfile(User user) {
        return userRepository.save(user);
    }
} 