package com.example.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 标签请求DTO
 */
@Data
public class TagRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String name;

    @Size(max = 7, message = "颜色值格式不正确")
    private String color = "#007bff";
} 