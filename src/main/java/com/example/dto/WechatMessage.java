package com.example.dto;

import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class WechatMessage {

    @XmlElement(name = "ToUserName")
    private String toUserName;

    @XmlElement(name = "FromUserName")
    private String fromUserName;

    @XmlElement(name = "CreateTime")
    private Long createTime;

    @XmlElement(name = "MsgType")
    private String msgType;

    @XmlElement(name = "MsgId")
    private Long msgId;

    @XmlElement(name = "Content")
    private String content;

    @XmlElement(name = "PicUrl")
    private String picUrl;

    @XmlElement(name = "MediaId")
    private String mediaId;

    @XmlElement(name = "Format")
    private String format;

    @XmlElement(name = "Recognition")
    private String recognition;

    @XmlElement(name = "ThumbMediaId")
    private String thumbMediaId;

    @XmlElement(name = "Location_X")
    private Double locationX;

    @XmlElement(name = "Location_Y")
    private Double locationY;

    @XmlElement(name = "Scale")
    private Integer scale;

    @XmlElement(name = "Label")
    private String label;

    @XmlElement(name = "Title")
    private String title;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "Url")
    private String url;

    @XmlElement(name = "Event")
    private String event;

    @XmlElement(name = "EventKey")
    private String eventKey;

    @XmlElement(name = "Ticket")
    private String ticket;

    @XmlElement(name = "Latitude")
    private Double latitude;

    @XmlElement(name = "Longitude")
    private Double longitude;

    @XmlElement(name = "Precision")
    private Double precision;
} 