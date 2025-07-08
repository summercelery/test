package com.example;

import com.example.dto.WechatMessage;
import com.example.service.WechatMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WechatMessageServiceTest {

    @Autowired
    private WechatMessageService wechatMessageService;

    @Test
    public void testTextMessage() {
        WechatMessage message = new WechatMessage();
        message.setToUserName("gh_test123");
        message.setFromUserName("o_test456");
        message.setCreateTime(System.currentTimeMillis() / 1000);
        message.setMsgType("text");
        message.setContent("你好");

        String reply = wechatMessageService.processMessage(message);
        assertNotNull(reply);
        assertTrue(reply.contains("你好！欢迎使用我们的服务！"));
    }

    @Test
    public void testHelpMessage() {
        WechatMessage message = new WechatMessage();
        message.setToUserName("gh_test123");
        message.setFromUserName("o_test456");
        message.setCreateTime(System.currentTimeMillis() / 1000);
        message.setMsgType("text");
        message.setContent("帮助");

        String reply = wechatMessageService.processMessage(message);
        assertNotNull(reply);
        assertTrue(reply.contains("可用命令"));
    }

    @Test
    public void testSubscribeEvent() {
        WechatMessage message = new WechatMessage();
        message.setToUserName("gh_test123");
        message.setFromUserName("o_test456");
        message.setCreateTime(System.currentTimeMillis() / 1000);
        message.setMsgType("event");
        message.setEvent("subscribe");

        String reply = wechatMessageService.processMessage(message);
        assertNotNull(reply);
        assertTrue(reply.contains("感谢关注"));
    }

    @Test
    public void testImageMessage() {
        WechatMessage message = new WechatMessage();
        message.setToUserName("gh_test123");
        message.setFromUserName("o_test456");
        message.setCreateTime(System.currentTimeMillis() / 1000);
        message.setMsgType("image");
        message.setPicUrl("http://example.com/image.jpg");

        String reply = wechatMessageService.processMessage(message);
        assertNotNull(reply);
        assertTrue(reply.contains("收到您的图片"));
    }

    @Test
    public void testVoiceMessage() {
        WechatMessage message = new WechatMessage();
        message.setToUserName("gh_test123");
        message.setFromUserName("o_test456");
        message.setCreateTime(System.currentTimeMillis() / 1000);
        message.setMsgType("voice");
        message.setRecognition("测试语音识别");

        String reply = wechatMessageService.processMessage(message);
        assertNotNull(reply);
        assertTrue(reply.contains("您说的是：测试语音识别"));
    }
} 