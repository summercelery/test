package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.TagRequest;
import com.example.entity.Tag;
import com.example.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 获取当前用户的标签列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Tag>>> getTags(Authentication authentication) {
        try {
            List<Tag> tags = tagService.getTagsByUser(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("获取标签列表成功", tags));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建标签
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Tag>> createTag(
            @Valid @RequestBody TagRequest tagRequest,
            Authentication authentication) {
        try {
            Tag tag = tagService.createTag(tagRequest, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("标签创建成功", tag));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Tag>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest tagRequest,
            Authentication authentication) {
        try {
            Tag tag = tagService.updateTag(id, tagRequest, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("标签更新成功", tag));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTag(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            boolean success = tagService.deleteTag(id, authentication.getName());
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("标签删除成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("标签删除失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 