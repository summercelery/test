package com.example.repository;

import com.example.entity.ReminderRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 提醒接收者数据访问层
 */
@Repository
public interface ReminderRecipientRepository extends JpaRepository<ReminderRecipient, Long> {

    /**
     * 根据提醒ID查找接收者列表
     */
    List<ReminderRecipient> findByReminderId(Long reminderId);

    /**
     * 根据提醒ID和状态查询接收者列表
     */
    List<ReminderRecipient> findByReminderIdAndStatus(Long reminderId, ReminderRecipient.Status status);

    /**
     * 根据用户ID查询该用户作为接收者的提醒
     */
    @Query("SELECT rr FROM ReminderRecipient rr WHERE rr.userId = :userId AND rr.isRegisteredUser = true")
    List<ReminderRecipient> findByUserId(@Param("userId") Long userId);

    /**
     * 根据手机号查询接收者
     */
    List<ReminderRecipient> findByPhoneNumber(String phoneNumber);

    /**
     * 根据微信OpenID查询接收者
     */
    List<ReminderRecipient> findByWechatOpenid(String wechatOpenid);

    /**
     * 根据邮箱查询接收者
     */
    List<ReminderRecipient> findByEmail(String email);

    /**
     * 根据提醒ID删除接收者
     */
    void deleteByReminderId(Long reminderId);

    /**
     * 统计提醒的接收者数量
     */
    long countByReminderId(Long reminderId);
} 