package com.example.service;

import com.example.dto.TagRequest;
import com.example.entity.Tag;
import com.example.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 标签服务类
 */
@Slf4j
@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    /**
     * 创建标签
     */
    @Transactional
    public Tag createTag(Long userId, TagRequest request) {
        // 检查标签名是否重复
        if (tagRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new RuntimeException("该标签名已存在");
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(request.getName());
        tag.setColor(request.getColor());

        Tag savedTag = tagRepository.save(tag);
        log.info("用户 {} 创建标签: {}", userId, savedTag.getId());
        return savedTag;
    }

    /**
     * 获取用户的标签列表
     */
    public List<Tag> getUserTags(Long userId) {
        return tagRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 搜索标签
     */
    public List<Tag> searchTags(Long userId, String name) {
        return tagRepository.findByUserIdAndNameContaining(userId, name);
    }

    /**
     * 获取标签详情
     */
    public Optional<Tag> getTagById(Long tagId, Long userId) {
        return tagRepository.findById(tagId)
                .filter(tag -> tag.getUserId().equals(userId));
    }

    /**
     * 更新标签
     */
    @Transactional
    public Tag updateTag(Long tagId, Long userId, TagRequest request) {
        Optional<Tag> tagOpt = getTagById(tagId, userId);
        if (!tagOpt.isPresent()) {
            throw new RuntimeException("标签不存在");
        }

        Tag tag = tagOpt.get();
        
        // 检查标签名是否重复（排除自己）
        Optional<Tag> existingTag = tagRepository.findByUserIdAndName(userId, request.getName());
        if (existingTag.isPresent() && !existingTag.get().getId().equals(tagId)) {
            throw new RuntimeException("该标签名已存在");
        }

        tag.setName(request.getName());
        tag.setColor(request.getColor());

        Tag updatedTag = tagRepository.save(tag);
        log.info("用户 {} 更新标签: {}", userId, tagId);
        return updatedTag;
    }

    /**
     * 删除标签
     */
    @Transactional
    public boolean deleteTag(Long tagId, Long userId) {
        Optional<Tag> tagOpt = getTagById(tagId, userId);
        if (tagOpt.isPresent()) {
            tagRepository.deleteById(tagId);
            log.info("用户 {} 删除标签: {}", userId, tagId);
            return true;
        }
        return false;
    }

    /**
     * 获取标签统计信息
     */
    public long getTagCount(Long userId) {
        return tagRepository.countByUserId(userId);
    }
} 