package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * 图形验证码服务
 */
@Slf4j
@Service
public class CaptchaService {

    @Autowired
    private RedisService redisService;

    private static final int CAPTCHA_WIDTH = 100;
    private static final int CAPTCHA_HEIGHT = 40;
    private static final int CAPTCHA_LENGTH = 4;
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;

    // 验证码字符集
    private static final String CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * 生成图形验证码
     */
    public CaptchaResult generateCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String captchaCode = generateCaptchaCode();
        
        // 存储验证码到Redis
        String redisKey = "captcha:" + captchaId;
        redisService.setValue(redisKey, captchaCode, CAPTCHA_EXPIRE_MINUTES * 60);
        
        // 生成图片
        String imageBase64 = generateCaptchaImage(captchaCode);
        
        return new CaptchaResult(captchaId, imageBase64);
    }

    /**
     * 验证图形验证码
     */
    public boolean verifyCaptcha(String captchaId, String captchaCode) {
        if (captchaId == null || captchaCode == null) {
            return false;
        }
        
        String redisKey = "captcha:" + captchaId;
        String storedCode = redisService.getValue(redisKey);
        
        if (storedCode != null && storedCode.equalsIgnoreCase(captchaCode)) {
            // 验证成功后删除验证码
            redisService.deleteKey(redisKey);
            log.info("图形验证码验证成功 - ID: {}", captchaId);
            return true;
        }
        
        log.warn("图形验证码验证失败 - ID: {}, 输入码: {}", captchaId, captchaCode);
        return false;
    }

    /**
     * 生成随机验证码文本
     */
    private String generateCaptchaCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            code.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        
        return code.toString();
    }

    /**
     * 生成验证码图片
     */
    private String generateCaptchaImage(String captchaCode) {
        BufferedImage image = new BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充背景
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT);
        
        // 绘制干扰线
        drawInterferenceLines(g2d);
        
        // 绘制验证码文本
        drawCaptchaText(g2d, captchaCode);
        
        g2d.dispose();
        
        // 转换为Base64
        return imageToBase64(image);
    }

    /**
     * 绘制干扰线
     */
    private void drawInterferenceLines(Graphics2D g2d) {
        Random random = new Random();
        
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(CAPTCHA_WIDTH);
            int y1 = random.nextInt(CAPTCHA_HEIGHT);
            int x2 = random.nextInt(CAPTCHA_WIDTH);
            int y2 = random.nextInt(CAPTCHA_HEIGHT);
            
            g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 绘制验证码文本
     */
    private void drawCaptchaText(Graphics2D g2d, String captchaCode) {
        Random random = new Random();
        Font font = new Font("Arial", Font.BOLD, 18);
        g2d.setFont(font);
        
        int x = 15;
        for (int i = 0; i < captchaCode.length(); i++) {
            // 随机颜色
            g2d.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            
            // 随机角度
            double angle = (random.nextDouble() - 0.5) * 0.5;
            g2d.rotate(angle, x, 25);
            
            // 绘制字符
            g2d.drawString(String.valueOf(captchaCode.charAt(i)), x, 25);
            
            // 重置旋转
            g2d.rotate(-angle, x, 25);
            
            x += 18;
        }
    }

    /**
     * 将图片转换为Base64
     */
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            log.error("图片转换Base64失败", e);
            throw new RuntimeException("生成验证码失败", e);
        }
    }

    /**
     * 验证码结果内部类
     */
    public static class CaptchaResult {
        private String captchaId;
        private String imageBase64;

        public CaptchaResult(String captchaId, String imageBase64) {
            this.captchaId = captchaId;
            this.imageBase64 = imageBase64;
        }

        public String getCaptchaId() {
            return captchaId;
        }

        public String getImageBase64() {
            return imageBase64;
        }
    }
} 