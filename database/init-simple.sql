-- =====================================================
-- 用户认证系统数据库初始化脚本（简化版）
-- 数据库名称: user_auth_system
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `user_auth_system` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `user_auth_system`;

-- 删除已存在的表
DROP TABLE IF EXISTS `users`;

-- 创建用户表
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `full_name` VARCHAR(100),
    `phone_number` VARCHAR(20),
    `role` ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `account_non_expired` BOOLEAN NOT NULL DEFAULT TRUE,
    `account_non_locked` BOOLEAN NOT NULL DEFAULT TRUE,
    `credentials_non_expired` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_phone_number` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入测试数据
-- 密码都是 123456 (BCrypt加密)

-- 管理员
INSERT INTO `users` (`username`, `password`, `email`, `full_name`, `phone_number`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@example.com', '系统管理员', '13800000000', 'ADMIN');

-- 普通用户
INSERT INTO `users` (`username`, `password`, `email`, `full_name`, `phone_number`, `role`) VALUES
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'zhangsan@example.com', '张三', '13811111111', 'USER'),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'lisi@example.com', '李四', '13822222222', 'USER'),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'test@example.com', '测试用户', '13999999999', 'USER');

-- 查看数据
SELECT `id`, `username`, `email`, `full_name`, `phone_number`, `role`, `enabled` FROM `users`; 