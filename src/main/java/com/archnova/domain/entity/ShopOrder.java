package com.archnova.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shop_order")
public class ShopOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long customerId;

    private Long productId;

    private String productTitle;

    private String payType;

    private Integer amountCent;

    private Integer pointsCost;

    /**
     * 0 待支付，1 已完成，2 已取消
     */
    private Integer status;

    private String sourceUrl;

    private String clientIp;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
