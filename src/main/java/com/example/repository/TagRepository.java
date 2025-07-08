package com.example.repository;

import com.example.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签数据访问层
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 根据用户ID查找标签列表
     */
    List<Tag> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据用户ID和名称查找标签
     */
    Optional<Tag> findByUserIdAndName(Long userId, String name);

    /**
     * 根据用户ID和名称模糊查询
     */
    @Query("SELECT t FROM Tag t WHERE t.userId = :userId AND t.name LIKE %:name%")
    List<Tag> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name);

    /**
     * 根据用户ID统计标签数量
     */
    long countByUserId(Long userId);

    /**
     * 检查用户是否已有同名标签
     */
    boolean existsByUserIdAndName(Long userId, String name);
} 