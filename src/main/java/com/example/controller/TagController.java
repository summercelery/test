package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.TagRequest;
import com.example.entity.Tag;
import com.example.service.TagService;
import com.example.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 标签控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://localhost:*", "https://127.0.0.1:*"}, allowCredentials = "true")
//@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 创建标签
     */
    @PostMapping
    public ResponseEntity<?> createTag(@Valid @RequestBody TagRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Tag tag = tagService.createTag(userId, request);
            return ResponseEntity.ok(ApiResponse.success("标签创建成功", tag));
        } catch (Exception e) {
            log.error("创建标签失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取标签列表
     */
    @GetMapping
    public ResponseEntity<?> getTags() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            List<Tag> tags = tagService.getUserTags(userId);
            return ResponseEntity.ok(ApiResponse.success("获取标签列表成功", tags));
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTags(@RequestParam String name) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            List<Tag> tags = tagService.searchTags(userId, name);
            return ResponseEntity.ok(ApiResponse.success("搜索标签成功", tags));
        } catch (Exception e) {
            log.error("搜索标签失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取标签详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTag(@PathVariable Long id) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            return tagService.getTagById(id, userId)
                    .map(tag -> ResponseEntity.ok(ApiResponse.success("获取标签详情成功", tag)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("获取标签详情失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTag(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Tag tag = tagService.updateTag(id, userId, request);
            return ResponseEntity.ok(ApiResponse.success("标签更新成功", tag));
        } catch (Exception e) {
            log.error("更新标签失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            boolean success = tagService.deleteTag(id, userId);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("标签删除成功"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("删除标签失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取标签统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getTagStats() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            long count = tagService.getTagCount(userId);
            return ResponseEntity.ok(ApiResponse.success("获取统计信息成功", count));
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 