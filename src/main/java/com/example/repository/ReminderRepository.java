package com.example.repository;

import com.example.entity.Reminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒数据访问层
 */
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * 根据用户ID查找提醒列表
     */
    Page<Reminder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据用户ID和状态查找提醒
     */
    List<Reminder> findByUserIdAndStatus(Long userId, Reminder.ReminderStatus status);

    /**
     * 根据用户ID和状态查找提醒列表（分页）
     */
    Page<Reminder> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Reminder.ReminderStatus status, Pageable pageable);

    /**
     * 根据用户ID搜索提醒（标题或内容包含关键词）
     */
    Page<Reminder> findByUserIdAndTitleContainingOrContentContainingOrderByCreatedAtDesc(
            Long userId, String title, String content, Pageable pageable);

    /**
     * 查找待发送的提醒（时间已到且状态为待发送）
     */
    @Query("SELECT r FROM Reminder r WHERE r.reminderTime <= :now AND r.status = 'PENDING'")
    List<Reminder> findPendingReminders(@Param("now") LocalDateTime now);

    /**
     * 根据用户ID统计提醒数量
     */
    long countByUserId(Long userId);

    /**
     * 根据用户ID和状态统计提醒数量
     */
    long countByUserIdAndStatus(Long userId, Reminder.ReminderStatus status);

    /**
     * 查找重复提醒（需要重新生成的提醒）
     */
    @Query("SELECT r FROM Reminder r WHERE r.repeatType != 'NONE' AND r.repeatEndTime > :now AND r.status = 'SENT'")
    List<Reminder> findRepeatingReminders(@Param("now") LocalDateTime now);
} 