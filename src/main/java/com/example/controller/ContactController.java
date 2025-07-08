package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.ContactRequest;
import com.example.entity.Contact;
import com.example.service.ContactService;
import com.example.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 提醒人控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://localhost:*", "https://127.0.0.1:*"}, allowCredentials = "true")
//@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ContactController {

    @Autowired
    private ContactService contactService;

    /**
     * 创建提醒人
     */
    @PostMapping
    public ResponseEntity<?> createContact(@Valid @RequestBody ContactRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Contact contact = contactService.createContact(userId, request);
            return ResponseEntity.ok(ApiResponse.success("提醒人创建成功", contact));
        } catch (Exception e) {
            log.error("创建提醒人失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取提醒人列表
     */
    @GetMapping
    public ResponseEntity<?> getContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            Page<Contact> contacts = contactService.getUserContacts(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success("获取提醒人列表成功", contacts));
        } catch (Exception e) {
            log.error("获取提醒人列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取所有提醒人（不分页）
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllContacts() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            List<Contact> contacts = contactService.getAllUserContacts(userId);
            return ResponseEntity.ok(ApiResponse.success("获取所有提醒人成功", contacts));
        } catch (Exception e) {
            log.error("获取所有提醒人失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据标签获取提醒人
     */
    @GetMapping("/by-tag/{tagId}")
    public ResponseEntity<?> getContactsByTag(@PathVariable Long tagId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            List<Contact> contacts = contactService.getContactsByTag(userId, tagId);
            return ResponseEntity.ok(ApiResponse.success("获取标签提醒人成功", contacts));
        } catch (Exception e) {
            log.error("获取标签提醒人失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 搜索提醒人
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchContacts(@RequestParam String name) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            List<Contact> contacts = contactService.searchContacts(userId, name);
            return ResponseEntity.ok(ApiResponse.success("搜索提醒人成功", contacts));
        } catch (Exception e) {
            log.error("搜索提醒人失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取提醒人详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getContact(@PathVariable Long id) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            return contactService.getContactById(id, userId)
                    .map(contact -> ResponseEntity.ok(ApiResponse.success("获取提醒人详情成功", contact)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("获取提醒人详情失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新提醒人
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Contact contact = contactService.updateContact(id, userId, request);
            return ResponseEntity.ok(ApiResponse.success("提醒人更新成功", contact));
        } catch (Exception e) {
            log.error("更新提醒人失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除提醒人
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            boolean success = contactService.deleteContact(id, userId);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("提醒人删除成功"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("删除提醒人失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取提醒人统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getContactStats() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            long count = contactService.getContactCount(userId);
            return ResponseEntity.ok(ApiResponse.success("获取统计信息成功", count));
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 