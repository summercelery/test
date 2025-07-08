package com.example.service;

import com.example.dto.WechatQRCodeRequest;
import com.example.dto.WechatQRCodeResponse;
import com.example.dto.WechatScanStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class WechatScanService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SCAN_PREFIX = "wechat:scan:";
    private static final String QR_CODE_PREFIX = "wechat:qrcode:";

    /**
     * 生成微信二维码
     */
    public WechatQRCodeResponse generateQRCode(WechatQRCodeRequest request) {
        String scanId = UUID.randomUUID().toString();
        String scene = request.getScene() != null ? request.getScene() : "reminder_scan";
        Integer expireSeconds = request.getExpireSeconds() != null ? request.getExpireSeconds() : 300;

        // 生成二维码URL（这里使用模拟URL，实际应该调用微信API）
        String qrCodeUrl = generateMockQRCodeUrl(scanId, scene);

        // 将扫码信息存储到Redis
        WechatScanStatusResponse scanStatus = new WechatScanStatusResponse();
        scanStatus.setScanId(scanId);
        scanStatus.setStatus("PENDING");

        String redisKey = SCAN_PREFIX + scanId;
        redisTemplate.opsForValue().set(redisKey, scanStatus, expireSeconds, TimeUnit.SECONDS);

        // 返回二维码信息
        WechatQRCodeResponse response = new WechatQRCodeResponse();
        response.setQrCodeUrl(qrCodeUrl);
        response.setScanId(scanId);
        response.setExpireSeconds(expireSeconds);

        return response;
    }

    /**
     * 获取扫码状态
     */
    public WechatScanStatusResponse getScanStatus(String scanId) {
        String redisKey = SCAN_PREFIX + scanId;
        Object cached = redisTemplate.opsForValue().get(redisKey);

        if (cached == null) {
            // 扫码已过期
            WechatScanStatusResponse expiredStatus = new WechatScanStatusResponse();
            expiredStatus.setScanId(scanId);
            expiredStatus.setStatus("EXPIRED");
            return expiredStatus;
        }

        return (WechatScanStatusResponse) cached;
    }

    /**
     * 模拟微信扫码回调（实际应该由微信服务器调用）
     */
    public void handleWechatScanCallback(String scanId, String openId, String nickname, String headimgurl) {
        String redisKey = SCAN_PREFIX + scanId;
        Object cached = redisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            WechatScanStatusResponse scanStatus = (WechatScanStatusResponse) cached;
            scanStatus.setStatus("SCANNED");
            scanStatus.setOpenId(openId);
            scanStatus.setNickname(nickname);
            scanStatus.setHeadimgurl(headimgurl);

            // 更新Redis中的状态
            redisTemplate.opsForValue().set(redisKey, scanStatus, 60, TimeUnit.SECONDS); // 扫码成功后保留1分钟
        }
    }

    /**
     * 生成模拟二维码URL
     */
    private String generateMockQRCodeUrl(String scanId, String scene) {
        // 实际项目中应该调用微信API生成二维码
        // 这里返回一个模拟的二维码图片URL
        return "data:image/svg+xml;base64," + generateMockQRCodeSVG(scanId, scene);
    }

    /**
     * 生成模拟二维码SVG
     */
    private String generateMockQRCodeSVG(String scanId, String scene) {
        // 生成一个简单的SVG二维码（实际项目中应该使用真实的二维码生成库）
        String svg = String.format(
            "<svg width='200' height='200' xmlns='http://www.w3.org/2000/svg'>" +
            "<rect width='200' height='200' fill='white'/>" +
            "<text x='100' y='100' text-anchor='middle' font-family='Arial' font-size='12' fill='black'>" +
            "模拟二维码</text>" +
            "<text x='100' y='120' text-anchor='middle' font-family='Arial' font-size='10' fill='gray'>" +
            "ScanID: %s</text>" +
            "<text x='100' y='135' text-anchor='middle' font-family='Arial' font-size='10' fill='gray'>" +
            "Scene: %s</text>" +
            "</svg>",
            scanId.substring(0, 8), scene
        );
        
        return java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    /**
     * 清理过期的扫码记录
     */
    public void cleanupExpiredScans() {
        // 这里可以添加定时任务清理过期的扫码记录
        // 由于使用了Redis的TTL，会自动清理过期数据
    }
} 