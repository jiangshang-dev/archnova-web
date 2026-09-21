package com.archnova.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shop_product")
public class ShopProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String cover;

    private String detailImages;

    private String contentMd;

    private Integer priceCent;

    private Integer pointsPrice;

    private String sourceUrl;

    private Integer sortNum;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
