package com.example.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 提醒人请求DTO
 */
@Data
public class ContactRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String name;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phoneNumber;

    @Size(max = 100, message = "微信OpenID长度不能超过100个字符")
    private String wechatOpenid;

    private List<Long> tagIds; // 关联的标签ID列表
} 