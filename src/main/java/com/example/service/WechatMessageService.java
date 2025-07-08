package com.example.service;

import com.example.dto.WechatMessage;
import com.example.dto.WechatReplyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class WechatMessageService {

    /**
     * 处理接收到的微信公众号消息
     */
    public String processMessage(WechatMessage message) {
        log.info("收到微信公众号消息: {}", message);
        
        WechatReplyMessage reply = new WechatReplyMessage();
        reply.setToUserName(message.getFromUserName());
        reply.setFromUserName(message.getToUserName());
        reply.setCreateTime(Instant.now().getEpochSecond());
        
        try {
            switch (message.getMsgType()) {
                case "text":
                    return handleTextMessage(message, reply);
                case "image":
                    return handleImageMessage(message, reply);
                case "voice":
                    return handleVoiceMessage(message, reply);
                case "video":
                    return handleVideoMessage(message, reply);
                case "location":
                    return handleLocationMessage(message, reply);
                case "link":
                    return handleLinkMessage(message, reply);
                case "event":
                    return handleEventMessage(message, reply);
                default:
                    return createTextReply(reply, "暂不支持该类型消息");
            }
        } catch (Exception e) {
            log.error("处理微信公众号消息失败", e);
            return createTextReply(reply, "消息处理失败，请稍后重试");
        }
    }

    /**
     * 处理文本消息
     */
    private String handleTextMessage(WechatMessage message, WechatReplyMessage reply) {
        String content = message.getContent();
        
        // 关键词回复
        if ("你好".equals(content) || "hello".equals(content.toLowerCase())) {
            return createTextReply(reply, "你好！欢迎使用我们的服务！");
        } else if ("帮助".equals(content) || "help".equals(content.toLowerCase())) {
            return createTextReply(reply, "可用命令：\n1. 你好 - 打招呼\n2. 帮助 - 查看帮助\n3. 天气 - 查看天气\n4. 新闻 - 查看新闻");
        } else if ("天气".equals(content)) {
            return createTextReply(reply, "今天天气晴朗，温度25°C，适合外出活动！");
        } else if ("新闻".equals(content)) {
            return createNewsReply(reply);
        } else {
            // 智能回复
            return createTextReply(reply, "收到您的消息：" + content + "\n我们会尽快为您处理！");
        }
    }

    /**
     * 处理图片消息
     */
    private String handleImageMessage(WechatMessage message, WechatReplyMessage reply) {
        return createTextReply(reply, "收到您的图片，图片链接：" + message.getPicUrl());
    }

    /**
     * 处理语音消息
     */
    private String handleVoiceMessage(WechatMessage message, WechatReplyMessage reply) {
        String recognition = message.getRecognition();
        if (recognition != null && !recognition.isEmpty()) {
            return createTextReply(reply, "您说的是：" + recognition);
        } else {
            return createTextReply(reply, "收到您的语音消息，但未能识别内容");
        }
    }

    /**
     * 处理视频消息
     */
    private String handleVideoMessage(WechatMessage message, WechatReplyMessage reply) {
        return createTextReply(reply, "收到您的视频消息");
    }

    /**
     * 处理位置消息
     */
    private String handleLocationMessage(WechatMessage message, WechatReplyMessage reply) {
        String location = String.format("您的位置：%s\n纬度：%.6f\n经度：%.6f", 
            message.getLabel(), message.getLocationX(), message.getLocationY());
        return createTextReply(reply, location);
    }

    /**
     * 处理链接消息
     */
    private String handleLinkMessage(WechatMessage message, WechatReplyMessage reply) {
        String link = String.format("收到您分享的链接：\n标题：%s\n描述：%s\n链接：%s", 
            message.getTitle(), message.getDescription(), message.getUrl());
        return createTextReply(reply, link);
    }

    /**
     * 处理事件消息
     */
    private String handleEventMessage(WechatMessage message, WechatReplyMessage reply) {
        switch (message.getEvent()) {
            case "subscribe":
                return createTextReply(reply, "感谢关注我们的公众号！");
            case "unsubscribe":
                log.info("用户取消关注: {}", message.getFromUserName());
                return "";
            case "CLICK":
                return handleClickEvent(message, reply);
            case "VIEW":
                return createTextReply(reply, "您点击了菜单链接");
            case "LOCATION":
                return createTextReply(reply, "收到您的位置信息");
            default:
                return createTextReply(reply, "收到事件：" + message.getEvent());
        }
    }

    /**
     * 处理点击事件
     */
    private String handleClickEvent(WechatMessage message, WechatReplyMessage reply) {
        String eventKey = message.getEventKey();
        switch (eventKey) {
            case "MENU_ABOUT":
                return createTextReply(reply, "关于我们：我们是一个专业的服务平台");
            case "MENU_CONTACT":
                return createTextReply(reply, "联系方式：\n电话：400-123-4567\n邮箱：support@example.com");
            case "MENU_SERVICE":
                return createTextReply(reply, "我们的服务：\n1. 在线咨询\n2. 技术支持\n3. 产品介绍");
            default:
                return createTextReply(reply, "您点击了菜单：" + eventKey);
        }
    }

    /**
     * 创建文本回复
     */
    private String createTextReply(WechatReplyMessage reply, String content) {
        reply.setMsgType("text");
        reply.setContent(content);
        return convertToXml(reply);
    }

    /**
     * 创建图文回复
     */
    private String createNewsReply(WechatReplyMessage reply) {
        reply.setMsgType("news");
        reply.setArticleCount(2);
        
        List<WechatReplyMessage.Article> articles = Arrays.asList(
            createArticle("今日头条", "最新新闻资讯", "https://example.com/news1.jpg", "https://example.com/news1"),
            createArticle("科技动态", "最新科技资讯", "https://example.com/news2.jpg", "https://example.com/news2")
        );
        
        reply.setArticles(articles);
        return convertToXml(reply);
    }

    /**
     * 创建图文消息
     */
    private WechatReplyMessage.Article createArticle(String title, String description, String picUrl, String url) {
        WechatReplyMessage.Article article = new WechatReplyMessage.Article();
        article.setTitle(title);
        article.setDescription(description);
        article.setPicUrl(picUrl);
        article.setUrl(url);
        return article;
    }

    /**
     * 将对象转换为XML字符串
     */
    private String convertToXml(Object obj) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            
            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            return writer.toString();
        } catch (JAXBException e) {
            log.error("XML转换失败", e);
            return "";
        }
    }
} 