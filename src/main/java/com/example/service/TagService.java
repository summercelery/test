package com.example.service;

import com.example.dto.TagRequest;
import com.example.entity.Tag;
import com.example.entity.User;
import com.example.repository.TagRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取用户的标签列表
     */
    public List<Tag> getTagsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return tagRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    /**
     * 获取标签统计信息
     */
    public long getTagCount(Long userId) {
        return tagRepository.countByUserId(userId);
    }
    /**
     * 创建标签
     */
    public Tag createTag(TagRequest tagRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查标签名称是否已存在
        if (tagRepository.existsByUserIdAndName(user.getId(), tagRequest.getName())) {
            throw new RuntimeException("标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setUserId(user.getId());
        tag.setName(tagRequest.getName());
        tag.setColor(tagRequest.getColor() != null ? tagRequest.getColor() : "#409EFF");
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());

        return tagRepository.save(tag);
    }
    /**
     * 获取用户的标签列表
     */
    public List<Tag> getUserTags(Long userId) {
        return tagRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 更新标签
     */
    public Tag updateTag(Long tagId, TagRequest tagRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));

        // 检查标签是否属于当前用户
        if (!tag.getUserId().equals(user.getId())) {
            throw new RuntimeException("无权限修改此标签");
        }

        // 检查标签名称是否已存在（排除当前标签）
        Optional<Tag> existingTag = tagRepository.findByUserIdAndName(user.getId(), tagRequest.getName());
        if (existingTag.isPresent() && !existingTag.get().getId().equals(tagId)) {
            throw new RuntimeException("标签名称已存在");
        }

        tag.setName(tagRequest.getName());
        tag.setColor(tagRequest.getColor());
        tag.setUpdatedAt(LocalDateTime.now());

        return tagRepository.save(tag);
    }

    /**
     * 删除标签
     */
    public boolean deleteTag(Long tagId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));

        // 检查标签是否属于当前用户
        if (!tag.getUserId().equals(user.getId())) {
            throw new RuntimeException("无权限删除此标签");
        }

        try {
            tagRepository.delete(tag);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除标签失败: " + e.getMessage());
        }
    }
} 