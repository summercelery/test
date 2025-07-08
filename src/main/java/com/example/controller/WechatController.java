package com.example.controller;

import com.example.dto.WechatMessage;
import com.example.service.WechatMessageService;
import com.example.service.WechatAccessTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;

@Slf4j
@RestController
@RequestMapping("/api/wechat")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://localhost:*", "https://127.0.0.1:*"}, allowCredentials = "true")
public class WechatController {

    @Autowired
    private WechatMessageService wechatMessageService;

    @Autowired
    private WechatAccessTokenService wechatAccessTokenService;

    @Value("${wechat.token:your_token}")
    private String token;

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    /**
     * 微信公众号服务器验证
     */
    @GetMapping("/callback")
    public String verifyServer(@RequestParam("signature") String signature,
                              @RequestParam("timestamp") String timestamp,
                              @RequestParam("nonce") String nonce,
                              @RequestParam("echostr") String echostr) {
        log.info("微信公众号服务器验证请求");
        
        try {
            if (checkSignature(signature, timestamp, nonce)) {
                log.info("微信公众号服务器验证成功");
                return echostr;
            } else {
                log.warn("微信公众号服务器验证失败");
                return "验证失败";
            }
        } catch (Exception e) {
            log.error("微信公众号服务器验证异常", e);
            return "验证异常";
        }
    }

    /**
     * 接收微信公众号消息
     */
    @PostMapping("/callback")
    public String receiveMessage(HttpServletRequest request) {
        log.info("收到微信公众号消息");
        
        try {
            // 读取请求体
            String xmlData = readRequestBody(request);
            log.info("接收到的XML数据: {}", xmlData);
            
            // 解析XML消息
            WechatMessage message = parseXmlMessage(xmlData);
            if (message == null) {
                log.error("解析XML消息失败");
                return "success";
            }
            
            // 处理消息并返回回复
            String reply = wechatMessageService.processMessage(message);
            log.info("返回的回复: {}", reply);
            
            return reply;
        } catch (Exception e) {
            log.error("处理微信公众号消息异常", e);
            return "success";
        }
    }

    /**
     * 检查签名
     */
    private boolean checkSignature(String signature, String timestamp, String nonce) {
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
    private String byteToHex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    /**
     * 读取请求体
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * 解析XML消息
     */
    private WechatMessage parseXmlMessage(String xmlData) {
        try {
            JAXBContext context = JAXBContext.newInstance(WechatMessage.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(xmlData);
            return (WechatMessage) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            log.error("解析XML消息失败", e);
            return null;
        }
    }

    /**
     * 获取微信公众号配置信息
     */
    @GetMapping("/config")
    public String getConfig() {
        return String.format("Token: %s, AppId: %s", token, appId);
    }

    /**
     * 获取Access Token
     */
    @GetMapping("/access-token")
    public String getAccessToken() {
        try {
            String accessToken = wechatAccessTokenService.getAccessToken();
            return String.format("Access Token: %s", accessToken);
        } catch (Exception e) {
            log.error("获取Access Token失败", e);
            return "获取Access Token失败: " + e.getMessage();
        }
    }

    /**
     * 刷新Access Token
     */
    @PostMapping("/refresh-token")
    public String refreshAccessToken() {
        try {
            String accessToken = wechatAccessTokenService.refreshAccessToken();
            return String.format("刷新Access Token成功: %s", accessToken);
        } catch (Exception e) {
            log.error("刷新Access Token失败", e);
            return "刷新Access Token失败: " + e.getMessage();
        }
    }

    /**
     * 检查Access Token有效性
     */
    @GetMapping("/check-token")
    public String checkAccessToken() {
        try {
            String accessToken = wechatAccessTokenService.getAccessToken();
            boolean isValid = wechatAccessTokenService.isAccessTokenValid(accessToken);
            return String.format("Access Token有效性: %s, Token: %s", isValid, accessToken);
        } catch (Exception e) {
            log.error("检查Access Token失败", e);
            return "检查Access Token失败: " + e.getMessage();
        }
    }
} 