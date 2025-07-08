package com.example.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;

@Slf4j
@Component
public class WechatUtil {

    /**
     * 验证微信公众号签名
     */
    public static boolean checkSignature(String token, String signature, String timestamp, String nonce) {
        try {
            String[] arr = {token, timestamp, nonce};
            Arrays.sort(arr);
            
            StringBuilder content = new StringBuilder();
            for (String item : arr) {
                content.append(item);
            }
            
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.toString().getBytes());
            String calculatedSignature = byteToHex(digest);
            
            return calculatedSignature.equals(signature);
        } catch (NoSuchAlgorithmException e) {
            log.error("计算签名失败", e);
            return false;
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String byteToHex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    /**
     * 生成随机字符串
     */
    public static String generateNonce() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 获取当前时间戳
     */
    public static String getTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 检查消息类型
     */
    public static boolean isValidMessageType(String msgType) {
        String[] validTypes = {"text", "image", "voice", "video", "location", "link", "event"};
        return Arrays.asList(validTypes).contains(msgType);
    }

    /**
     * 检查事件类型
     */
    public static boolean isValidEventType(String event) {
        String[] validEvents = {"subscribe", "unsubscribe", "CLICK", "VIEW", "LOCATION", "SCAN"};
        return Arrays.asList(validEvents).contains(event);
    }
} 