package com.example.dto;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class WechatReplyMessage {

    @XmlElement(name = "ToUserName")
    private String toUserName;

    @XmlElement(name = "FromUserName")
    private String fromUserName;

    @XmlElement(name = "CreateTime")
    private Long createTime;

    @XmlElement(name = "MsgType")
    private String msgType;

    @XmlElement(name = "Content")
    private String content;

    @XmlElement(name = "Image")
    private Image image;

    @XmlElement(name = "Voice")
    private Voice voice;

    @XmlElement(name = "Video")
    private Video video;

    @XmlElement(name = "Music")
    private Music music;

    @XmlElement(name = "ArticleCount")
    private Integer articleCount;

    @XmlElementWrapper(name = "Articles")
    @XmlElement(name = "item")
    private List<Article> articles;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Image {
        @XmlElement(name = "MediaId")
        private String mediaId;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Voice {
        @XmlElement(name = "MediaId")
        private String mediaId;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Video {
        @XmlElement(name = "MediaId")
        private String mediaId;

        @XmlElement(name = "Title")
        private String title;

        @XmlElement(name = "Description")
        private String description;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Music {
        @XmlElement(name = "Title")
        private String title;

        @XmlElement(name = "Description")
        private String description;

        @XmlElement(name = "MusicUrl")
        private String musicUrl;

        @XmlElement(name = "HQMusicUrl")
        private String hqMusicUrl;

        @XmlElement(name = "ThumbMediaId")
        private String thumbMediaId;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Article {
        @XmlElement(name = "Title")
        private String title;

        @XmlElement(name = "Description")
        private String description;

        @XmlElement(name = "PicUrl")
        private String picUrl;

        @XmlElement(name = "Url")
        private String url;
    }
} 