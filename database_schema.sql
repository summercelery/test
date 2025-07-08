-- 用户认证系统数据库建表脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS user_auth_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE user_auth_system;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    wechat_openid VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_phone_number (phone_number),
    INDEX idx_email (email),
    INDEX idx_wechat_openid (wechat_openid)
);

-- 提醒表
CREATE TABLE IF NOT EXISTS reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    reminder_time DATETIME NOT NULL,
    reminder_types JSON NOT NULL,
    repeat_type ENUM('NONE', 'DAILY', 'WEEKLY', 'MONTHLY') DEFAULT 'NONE',
    repeat_end_time DATETIME,
    status ENUM('PENDING', 'SENT', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_reminder_time (reminder_time),
    INDEX idx_status (status),
    INDEX idx_user_status (user_id, status)
);

-- 提醒接收者表
CREATE TABLE IF NOT EXISTS reminder_recipients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reminder_id BIGINT NOT NULL,
    recipient_type ENUM('PHONE', 'WECHAT') NOT NULL,
    recipient_value VARCHAR(100) NOT NULL,
    name VARCHAR(50),
    phone_number VARCHAR(20),
    wechat_openid VARCHAR(100),
    email VARCHAR(100),
    relationship VARCHAR(50),
    is_registered_user BOOLEAN DEFAULT FALSE,
    user_id BIGINT,
    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') DEFAULT 'ACTIVE',
    last_sent_time DATETIME,
    sent_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reminder_id) REFERENCES reminders(id) ON DELETE CASCADE,
    INDEX idx_reminder_id (reminder_id),
    INDEX idx_recipient_type (recipient_type),
    INDEX idx_status (status),
    INDEX idx_reminder_status (reminder_id, status)
);

-- 提醒人表
CREATE TABLE IF NOT EXISTS contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    wechat_openid VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    INDEX idx_phone_number (phone_number),
    INDEX idx_wechat_openid (wechat_openid),
    UNIQUE KEY unique_user_name (user_id, name)
);

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) DEFAULT '#007bff',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    UNIQUE KEY unique_user_tag (user_id, name)
);

-- 提醒人标签关联表
CREATE TABLE IF NOT EXISTS contact_tags (
    contact_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (contact_id, tag_id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    INDEX idx_contact_id (contact_id),
    INDEX idx_tag_id (tag_id)
);

-- 插入测试数据
INSERT INTO users (username, password, phone_number, email, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '13800138000', 'admin@example.com', 'ADMIN'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '13800138001', 'user1@example.com', 'USER'),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '13800138002', 'user2@example.com', 'USER')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 插入测试标签
INSERT INTO tags (user_id, name, color) VALUES
(2, '家人', '#28a745'),
(2, '朋友', '#007bff'),
(2, '同事', '#ffc107'),
(3, '重要', '#dc3545'),
(3, '一般', '#6c757d')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 插入测试提醒人
INSERT INTO contacts (user_id, name, phone_number, wechat_openid) VALUES
(2, '张三', '13900139001', 'wx_openid_001'),
(2, '李四', '13900139002', 'wx_openid_002'),
(2, '王五', '13900139003', NULL),
(3, '赵六', '13900139004', 'wx_openid_003'),
(3, '钱七', NULL, 'wx_openid_004')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 插入提醒人标签关联
INSERT INTO contact_tags (contact_id, tag_id) VALUES
(1, 1), -- 张三 - 家人
(2, 2), -- 李四 - 朋友
(3, 3), -- 王五 - 同事
(4, 4), -- 赵六 - 重要
(5, 5)  -- 钱七 - 一般
ON DUPLICATE KEY UPDATE contact_id = contact_id;

-- 插入测试提醒
INSERT INTO reminders (user_id, title, content, reminder_time, reminder_types, repeat_type, status) VALUES
(2, '会议提醒', '下午2点有重要会议', DATE_ADD(NOW(), INTERVAL 1 HOUR), '["WECHAT", "SMS"]', 'NONE', 'PENDING'),
(2, '生日提醒', '明天是张三的生日', DATE_ADD(NOW(), INTERVAL 1 DAY), '["WECHAT"]', 'YEARLY', 'PENDING'),
(3, '吃药提醒', '记得按时吃药', DATE_ADD(NOW(), INTERVAL 30 MINUTE), '["SMS", "PHONE"]', 'DAILY', 'PENDING')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 插入提醒接收者
INSERT INTO reminder_recipients (reminder_id, recipient_type, recipient_value) VALUES
(1, 'WECHAT', 'wx_openid_001'),
(1, 'SMS', '13900139001'),
(2, 'WECHAT', 'wx_openid_002'),
(3, 'SMS', '13900139004'),
(3, 'PHONE', '13900139004')
ON DUPLICATE KEY UPDATE reminder_id = reminder_id; 