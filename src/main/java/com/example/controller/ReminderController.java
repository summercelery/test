package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.ReminderRequest;
import com.example.dto.ReminderResponse;
import com.example.entity.Reminder;
import com.example.service.ReminderService;
import com.example.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提醒控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://localhost:*", "https://127.0.0.1:*"}, allowCredentials = "true")
// @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    /**
     * 创建提醒
     */
    @PostMapping
    public ResponseEntity<?> createReminder(@Valid @RequestBody ReminderRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Reminder reminder = reminderService.createReminder(userId, request);
            ReminderResponse response = convertToResponse(reminder);
            return ResponseEntity.ok(ApiResponse.success("提醒创建成功", response));
        } catch (Exception e) {
            log.error("创建提醒失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取提醒列表
     */
    @GetMapping
    public ResponseEntity<?> getReminders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            Page<Reminder> reminders;
            if (status != null && !status.isEmpty()) {
                Reminder.ReminderStatus reminderStatus = Reminder.ReminderStatus.valueOf(status);
                reminders = reminderService.getUserRemindersByStatus(userId, reminderStatus, pageable);
            } else if (search != null && !search.isEmpty()) {
                reminders = reminderService.searchUserReminders(userId, search, pageable);
            } else {
                reminders = reminderService.getUserReminders(userId, pageable);
            }
            
            Page<ReminderResponse> response = reminders.map(this::convertToResponse);
            return ResponseEntity.ok(ApiResponse.success("获取提醒列表成功", response));
        } catch (Exception e) {
            log.error("获取提醒列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取提醒详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReminder(@PathVariable Long id) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Reminder reminder = reminderService.getReminderById(id, userId);
            if (reminder != null) {
                ReminderResponse response = convertToResponse(reminder);
                return ResponseEntity.ok(ApiResponse.success("获取提醒详情成功", response));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取提醒详情失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新提醒
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReminder(@PathVariable Long id, @Valid @RequestBody ReminderRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Reminder reminder = reminderService.updateReminder(userId, id, request);
            ReminderResponse response = convertToResponse(reminder);
            return ResponseEntity.ok(ApiResponse.success("提醒更新成功", response));
        } catch (Exception e) {
            log.error("更新提醒失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 取消提醒
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReminder(@PathVariable Long id) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            boolean success = reminderService.cancelReminder(id, userId);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("提醒取消成功"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("取消提醒失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除提醒
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReminder(@PathVariable Long id) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            boolean success = reminderService.deleteReminder(id, userId);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("提醒删除成功"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("删除提醒失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取提醒统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getReminderStats() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            ReminderService.ReminderStats stats = reminderService.getReminderStats(userId);
            return ResponseEntity.ok(ApiResponse.success("获取统计信息成功", stats));
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取提醒类型列表
     */
    @GetMapping("/types")
    public ApiResponse<Map<String, Object>> getReminderTypes() {
        try {
            Map<String, Object> types = new HashMap<>();
            
            // 提醒类型
            Map<String, String> reminderTypes = new HashMap<>();
            for (Reminder.ReminderType type : Reminder.ReminderType.values()) {
                reminderTypes.put(type.name(), type.getDescription());
            }
            types.put("reminderTypes", reminderTypes);
            
            // 重复类型
            Map<String, String> repeatTypes = new HashMap<>();
            for (Reminder.RepeatType type : Reminder.RepeatType.values()) {
                repeatTypes.put(type.name(), type.getDescription());
            }
            types.put("repeatTypes", repeatTypes);
            
            return ApiResponse.success("获取类型列表成功", types);
        } catch (Exception e) {
            log.error("获取类型列表失败", e);
            return ApiResponse.error("获取类型列表失败: " + e.getMessage());
        }
    }

    /**
     * 转换为响应对象
     */
    private ReminderResponse convertToResponse(Reminder reminder) {
        ReminderResponse response = new ReminderResponse();
        response.setId(reminder.getId());
        response.setTitle(reminder.getTitle());
        response.setContent(reminder.getContent());
        response.setReminderTime(reminder.getReminderTime());
        
        // 处理多选提醒类型
        if (reminder.getReminderTypes() != null && !reminder.getReminderTypes().isEmpty()) {
            String typesDescription = reminder.getReminderTypes().stream()
                    .map(type -> Reminder.ReminderType.valueOf(type).getDescription())
                    .collect(Collectors.joining("、"));
            response.setReminderType(typesDescription);
        } else {
            response.setReminderType("未设置");
        }
        
        // 设置业务类型
        response.setType(reminder.getType());
        
        response.setStatus(reminder.getStatus().getDescription());
        response.setRepeatType(reminder.getRepeatType().getDescription());
        response.setRepeatInterval(reminder.getRepeatInterval());
        response.setRepeatEndTime(reminder.getRepeatEndTime());
        response.setCreatedAt(reminder.getCreatedAt());
        response.setLastSentTime(reminder.getLastSentTime());
        response.setSentCount(reminder.getSentCount());

        return response;
    }
} 