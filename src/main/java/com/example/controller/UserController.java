package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.service.ReminderService;
import com.example.service.ContactService;
import com.example.service.TagService;
import com.example.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://localhost:*", "https://127.0.0.1:*"}, allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ReminderService reminderService;
    
    @Autowired
    private ContactService contactService;
    
    @Autowired
    private TagService tagService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            // 清除敏感信息
            user.setPassword(null);
            
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<String>> hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success("欢迎", "Hello " + username + "!"));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            
            // 获取各种统计数据
            ReminderService.ReminderStats reminderStats = reminderService.getReminderStats(userId);
            long contactCount = contactService.getContactCount(userId);
            long tagCount = tagService.getTagCount(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalReminders", reminderStats.getTotal());
            stats.put("pendingReminders", reminderStats.getPending());
            stats.put("sentReminders", reminderStats.getSent());
            stats.put("cancelledReminders", reminderStats.getCancelled());
            stats.put("contactCount", contactCount);
            stats.put("tagCount", tagCount);
            
            return ResponseEntity.ok(ApiResponse.success("获取用户统计信息成功", stats));
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> profileData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            // 更新用户信息
            if (profileData.containsKey("fullName")) {
                user.setFullName((String) profileData.get("fullName"));
            }
            if (profileData.containsKey("email")) {
                user.setEmail((String) profileData.get("email"));
            }
            if (profileData.containsKey("phoneNumber")) {
                user.setPhoneNumber((String) profileData.get("phoneNumber"));
            }
            
            User updatedUser = userService.updateUserProfile(user);
            
            // 清除敏感信息
            updatedUser.setPassword(null);
            
            return ResponseEntity.ok(ApiResponse.success("更新用户信息成功", updatedUser));
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 