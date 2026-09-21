package com.archnova.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("contact_message")
public class ContactMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String mobile;

    private String email;

    private String company;

    private String serviceType;

    private String content;

    private String clientIp;

    private Integer status;

    private LocalDateTime createTime;
}
