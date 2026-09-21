package com.archnova.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_session")
public class ChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String visitorToken;

    private String visitorName;

    private String clientIp;

    private String lastMessage;

    private Integer unreadCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
