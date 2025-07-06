-- =====================================================
-- 用户认证系统数据库初始化脚本
-- 数据库名称: user_auth_system
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `user_auth_system` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `user_auth_system`;

-- =====================================================
-- 删除已存在的表（如果存在）
-- =====================================================
DROP TABLE IF EXISTS `users`;

-- =====================================================
-- 创建用户表
-- =====================================================
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，自增主键',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一',
    `password` VARCHAR(255) NOT NULL COMMENT '密码，BCrypt加密',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱地址，唯一',
    `full_name` VARCHAR(100) COMMENT '用户全名',
    `phone_number` VARCHAR(20) COMMENT '手机号码',
    `role` ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色：USER普通用户，ADMIN管理员',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否启用',
    `account_non_expired` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否未过期',
    `account_non_locked` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否未锁定',
    `credentials_non_expired` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '凭据是否未过期',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_phone_number` (`phone_number`),
    INDEX `idx_role` (`role`),
    INDEX `idx_enabled` (`enabled`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 插入测试数据
-- =====================================================

-- 注意：密码使用BCrypt加密，原始密码都是 123456
-- 可以使用在线BCrypt生成器或Spring Security的BCryptPasswordEncoder生成

-- 1. 管理员用户
INSERT INTO `users` (
    `username`, 
    `password`, 
    `email`, 
    `full_name`, 
    `phone_number`, 
    `role`, 
    `enabled`
) VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- 123456
    'admin@example.com',
    '系统管理员',
    '13800000000',
    'ADMIN',
    TRUE
);

-- 2. 普通用户1
INSERT INTO `users` (
    `username`, 
    `password`, 
    `email`, 
    `full_name`, 
    `phone_number`, 
    `role`, 
    `enabled`
) VALUES (
    'zhangsan',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- 123456
    'zhangsan@example.com',
    '张三',
    '13811111111',
    'USER',
    TRUE
);

-- 3. 普通用户2
INSERT INTO `users` (
    `username`, 
    `password`, 
    `email`, 
    `full_name`, 
    `phone_number`, 
    `role`, 
    `enabled`
) VALUES (
    'lisi',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- 123456
    'lisi@example.com',
    '李四',
    '13822222222',
    'USER',
    TRUE
);

-- 4. 普通用户3
INSERT INTO `users` (
    `username`, 
    `password`, 
    `email`,
    `full_name`, 
    `phone_number`, 
    `role`, 
    `enabled`
) VALUES (
    'wangwu',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- 123456
    'wangwu@example.com',
    '王五',
    '13833333333',
    'USER',
    TRUE
);

-- 5. 普通用户4
INSERT INTO `users` (
    `username`, 
    `password`, 
    `email`, 
    `full_name`, 
    `phone_number`,
    `role`, 
    `enabled`
) VALUES (
    'zhaoliu',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- 123456
    'zhaoliu@example.com',
    '赵六',
    '13866666666',
    'USER',
    TRUE
);

-- 6. 测试用户（用于前端测试）
INSERT INTO `users` (
    `username`, 
    `password`, 
    `email`, 
    `full_name`, 
    `phone_number`, 
    `role`, 
    `enabled`
) VALUES (
    'testuser',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- 123456
    'test@example.com',
    '测试用户',
    '13999999999',
    'USER',
    TRUE
);

-- 7. 禁用用户（用于测试账户状态）
INSERT INTO `users` (
    `username`, 
    `password`, 
    `email`, 
    `full_name`, 
    `phone_number`, 
    `role`, 
    `enabled`
) VALUES (
    'disabled_user',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- 123456
    'disabled@example.com',
    '禁用用户',
    '13888888888',
    'USER',
    FALSE
);

-- =====================================================
-- 创建视图（可选）
-- =====================================================

-- 活跃用户视图
CREATE OR REPLACE VIEW `active_users` AS
SELECT 
    `id`,
    `username`,
    `email`,
    `full_name`,
    `phone_number`,
    `role`,
    `created_at`,
    `updated_at`
FROM `users`
WHERE `enabled` = TRUE 
  AND `account_non_expired` = TRUE 
  AND `account_non_locked` = TRUE 
  AND `credentials_non_expired` = TRUE;

-- 用户统计视图
CREATE OR REPLACE VIEW `user_stats` AS
SELECT 
    `role`,
    COUNT(*) as `total_count`,
    SUM(CASE WHEN `enabled` = TRUE THEN 1 ELSE 0 END) as `enabled_count`,
    SUM(CASE WHEN `enabled` = FALSE THEN 1 ELSE 0 END) as `disabled_count`,
    MIN(`created_at`) as `earliest_registration`,
    MAX(`created_at`) as `latest_registration`
FROM `users`
GROUP BY `role`;

-- =====================================================
-- 创建存储过程（可选）
-- =====================================================

DELIMITER //

-- 获取用户统计信息的存储过程
CREATE PROCEDURE `GetUserStatistics`()
BEGIN
    SELECT 
        COUNT(*) as `total_users`,
        SUM(CASE WHEN `role` = 'ADMIN' THEN 1 ELSE 0 END) as `admin_count`,
        SUM(CASE WHEN `role` = 'USER' THEN 1 ELSE 0 END) as `user_count`,
        SUM(CASE WHEN `enabled` = TRUE THEN 1 ELSE 0 END) as `enabled_users`,
        SUM(CASE WHEN `enabled` = FALSE THEN 1 ELSE 0 END) as `disabled_users`,
        MIN(`created_at`) as `earliest_user`,
        MAX(`created_at`) as `latest_user`
    FROM `users`;
END //

-- 搜索用户的存储过程
CREATE PROCEDURE `SearchUsers`(
    IN `search_term` VARCHAR(100)
)
BEGIN
    SELECT 
        `id`,
        `username`,
        `email`,
        `full_name`,
        `phone_number`,
        `role`,
        `enabled`,
        `created_at`
    FROM `users`
    WHERE `username` LIKE CONCAT('%', `search_term`, '%')
       OR `email` LIKE CONCAT('%', `search_term`, '%')
       OR `full_name` LIKE CONCAT('%', `search_term`, '%')
       OR `phone_number` LIKE CONCAT('%', `search_term`, '%')
    ORDER BY `created_at` DESC;
END //

DELIMITER ;

-- =====================================================
-- 创建触发器（可选）
-- =====================================================

-- 用户更新日志触发器
CREATE TABLE `user_audit_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `action` VARCHAR(20) NOT NULL COMMENT '操作类型：INSERT, UPDATE, DELETE',
    `old_values` JSON COMMENT '更新前的值',
    `new_values` JSON COMMENT '更新后的值',
    `changed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `changed_by` VARCHAR(50) COMMENT '操作人'
);

DELIMITER //

CREATE TRIGGER `user_audit_insert` 
AFTER INSERT ON `users`
FOR EACH ROW
BEGIN
    INSERT INTO `user_audit_log` (`user_id`, `action`, `new_values`)
    VALUES (NEW.id, 'INSERT', JSON_OBJECT(
        'username', NEW.username,
        'email', NEW.email,
        'full_name', NEW.full_name,
        'phone_number', NEW.phone_number,
        'role', NEW.role,
        'enabled', NEW.enabled
    ));
END //

CREATE TRIGGER `user_audit_update` 
AFTER UPDATE ON `users`
FOR EACH ROW
BEGIN
    INSERT INTO `user_audit_log` (`user_id`, `action`, `old_values`, `new_values`)
    VALUES (NEW.id, 'UPDATE', 
        JSON_OBJECT(
            'username', OLD.username,
            'email', OLD.email,
            'full_name', OLD.full_name,
            'phone_number', OLD.phone_number,
            'role', OLD.role,
            'enabled', OLD.enabled
        ),
        JSON_OBJECT(
            'username', NEW.username,
            'email', NEW.email,
            'full_name', NEW.full_name,
            'phone_number', NEW.phone_number,
            'role', NEW.role,
            'enabled', NEW.enabled
        )
    );
END //

DELIMITER ;

-- =====================================================
-- 验证数据
-- =====================================================

-- 查看所有用户
SELECT 
    `id`,
    `username`,
    `email`,
    `full_name`,
    `phone_number`,
    `role`,
    `enabled`,
    `created_at`
FROM `users`
ORDER BY `id`;

-- 查看用户统计
CALL `GetUserStatistics`();

-- 查看活跃用户
SELECT * FROM `active_users`;

-- 查看用户统计视图
SELECT * FROM `user_stats`;

-- =====================================================
-- 使用说明
-- =====================================================

/*
使用说明：

1. 数据库连接信息：
   - 数据库名：user_auth_system
   - 字符集：utf8mb4
   - 排序规则：utf8mb4_unicode_ci

2. 测试账号：
   - 管理员：admin / 123456
   - 普通用户：zhangsan / 123456
   - 普通用户：lisi / 123456
   - 普通用户：wangwu / 123456
   - 普通用户：zhaoliu / 123456
   - 测试用户：testuser / 123456
   - 禁用用户：disabled_user / 123456

3. 密码说明：
   - 所有测试用户的密码都是：123456
   - 密码使用BCrypt加密存储

4. 角色说明：
   - ADMIN：管理员，拥有所有权限
   - USER：普通用户，基本权限

5. 字段说明：
   - enabled：账户是否启用
   - account_non_expired：账户是否未过期
   - account_non_locked：账户是否未锁定
   - credentials_non_expired：凭据是否未过期

6. 索引说明：
   - username：用户名唯一索引
   - email：邮箱唯一索引
   - phone_number：手机号索引
   - role：角色索引
   - enabled：启用状态索引
   - created_at：创建时间索引

7. 视图说明：
   - active_users：活跃用户视图
   - user_stats：用户统计视图

8. 存储过程说明：
   - GetUserStatistics：获取用户统计信息
   - SearchUsers：搜索用户

9. 触发器说明：
   - user_audit_insert：用户创建审计日志
   - user_audit_update：用户更新审计日志
*/ 