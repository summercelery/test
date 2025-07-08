package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.WechatQRCodeRequest;
import com.example.dto.WechatQRCodeResponse;
import com.example.dto.WechatScanStatusResponse;
import com.example.service.WechatScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wechat")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://localhost:*", "https://127.0.0.1:*"}, allowCredentials = "true")
public class WechatScanController {

    @Autowired
    private WechatScanService wechatScanService;

    /**
     * 生成微信二维码
     */
    @PostMapping("/qr-code")
    public ApiResponse<WechatQRCodeResponse> generateQRCode(@RequestBody WechatQRCodeRequest request) {
        try {
            WechatQRCodeResponse response = wechatScanService.generateQRCode(request);
            return ApiResponse.success("二维码生成成功", response);
        } catch (Exception e) {
            return ApiResponse.error("生成二维码失败: " + e.getMessage());
        }
    }

    /**
     * 获取扫码状态
     */
    @GetMapping("/scan-status/{scanId}")
    public ApiResponse<WechatScanStatusResponse> getScanStatus(@PathVariable String scanId) {
        try {
            WechatScanStatusResponse response = wechatScanService.getScanStatus(scanId);
            return ApiResponse.success("获取扫码状态成功", response);
        } catch (Exception e) {
            return ApiResponse.error("获取扫码状态失败: " + e.getMessage());
        }
    }

    /**
     * 模拟微信扫码回调（用于测试）
     */
    @PostMapping("/scan-callback")
    public ApiResponse<String> handleScanCallback(
            @RequestParam String scanId,
            @RequestParam String openId,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String headimgurl) {
        try {
            wechatScanService.handleWechatScanCallback(scanId, openId, nickname, headimgurl);
            return ApiResponse.success("扫码回调处理成功");
        } catch (Exception e) {
            return ApiResponse.error("处理扫码回调失败: " + e.getMessage());
        }
    }
} 