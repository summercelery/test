package com.example.repository;

import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    Optional<User> findByWechatOpenid(String wechatOpenid);
    
    Optional<User> findByWechatUnionid(String wechatUnionid);
    
    boolean existsByWechatOpenid(String wechatOpenid);
    
    boolean existsByWechatUnionid(String wechatUnionid);
} 