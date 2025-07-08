package com.example.repository;

import com.example.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提醒人数据访问层
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /**
     * 根据用户ID查找提醒人列表
     */
    Page<Contact> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据用户ID查找所有提醒人
     */
    List<Contact> findByUserId(Long userId);

    /**
     * 根据用户ID和姓名查找提醒人
     */
    Optional<Contact> findByUserIdAndName(Long userId, String name);

    /**
     * 根据用户ID和手机号查找提醒人
     */
    Optional<Contact> findByUserIdAndPhoneNumber(Long userId, String phoneNumber);

    /**
     * 根据用户ID和微信OpenID查找提醒人
     */
    Optional<Contact> findByUserIdAndWechatOpenid(Long userId, String wechatOpenid);

    /**
     * 根据用户ID和标签ID查找提醒人
     */
    @Query("SELECT c FROM Contact c JOIN c.tags t WHERE c.userId = :userId AND t.id = :tagId")
    List<Contact> findByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    /**
     * 根据用户ID和姓名模糊查询
     */
    @Query("SELECT c FROM Contact c WHERE c.userId = :userId AND c.name LIKE CONCAT('%', :name, '%')")
    List<Contact> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name);

    /**
     * 根据用户ID统计提醒人数量
     */
    long countByUserId(Long userId);
} 