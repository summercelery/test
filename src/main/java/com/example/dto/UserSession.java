package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession implements Serializable {
    
    private String userId;
    private String username;
    private String phoneNumber;
    private String email;
    private String fullName;
    private String role;
    private String token;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
    private String ipAddress;
    private String userAgent;
    
    public UserSession(String userId, String username, String role, String token) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.token = token;
        this.loginTime = LocalDateTime.now();
        this.lastAccessTime = LocalDateTime.now();
    }
    
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }
} 