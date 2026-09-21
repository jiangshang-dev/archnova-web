package com.archnova.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.archnova.common.PageResult;
import com.archnova.domain.entity.ShopCustomer;
import com.archnova.domain.entity.ShopOrder;
import com.archnova.domain.entity.ShopProduct;
import com.archnova.mapper.ShopCustomerMapper;
import com.archnova.mapper.ShopOrderMapper;
import com.archnova.mq.OrderDelayPublisher;
import com.archnova.utils.AssertUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShopOrderService {

    public static final int PENDING = 0;
    public static final int PAID = 1;
    public static final int CANCELLED = 2;
    private static final Set<String> PAY_TYPES = Set.of("POINTS", "ALIPAY", "WECHAT");

    private final ShopOrderMapper shopOrderMapper;
    private final ShopCustomerMapper shopCustomerMapper;
    private final ShopProductService shopProductService;
    private final ShopCustomerService shopCustomerService;
    private final OrderDelayPublisher orderDelayPublisher;

    @Value("${archnova.pay.alipay.app-id:}")
    private String alipayAppId;

    @Value("${archnova.pay.wechat.mch-id:}")
    private String wechatMchId;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(Long productId, String payType, String clientIp) {
        ShopCustomer customer = shopCustomerService.current();
        String type = StrUtil.blankToDefault(payType, "").toUpperCase();
        AssertUtil.isTrue(PAY_TYPES.contains(type), "请选择积分、支付宝或微信");
        ShopProduct product = shopProductService.requireOnSale(productId);
        ShopOrder order = new ShopOrder();
        order.setOrderNo(nextOrderNo());
        order.setCustomerId(customer.getId());
        order.setProductId(product.getId());
        order.setProductTitle(product.getTitle());
        order.setPayType(type);
        order.setAmountCent(product.getPriceCent() == null ? 0 : product.getPriceCent());
        order.setPointsCost(product.getPointsPrice() == null ? 0 : product.getPointsPrice());
        order.setStatus(PENDING);
        order.setClientIp(clientIp);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        if ("POINTS".equals(type)) {
            AssertUtil.isTrue(order.getPointsCost() > 0, "该商品不支持积分支付");
            deductPoints(customer.getId(), order.getPointsCost());
            order.setStatus(PAID);
            order.setSourceUrl(product.getSourceUrl());
            order.setPayTime(LocalDateTime.now());
            shopOrderMapper.insert(order);
            return view(order, "积分支付成功，源码地址已发放");
        }
        AssertUtil.isTrue(order.getAmountCent() > 0, "该商品未设置现金价格");
        shopOrderMapper.insert(order);
        orderDelayPublisher.publish(order.getOrderNo());
        String tip = channelReady(type)
                ? "订单已创建，请完成支付。超过 24 小时未支付将自动取消。"
                : "订单已创建为待支付。支付宝或微信商户参数还未配置，配置回调地址后才会变成已完成；超过 24 小时未支付将自动取消。";
        return view(order, tip);
    }

    public PageResult<ShopOrder> myOrders(long page, long size, Integer status) {
        ShopCustomer customer = shopCustomerService.current();
        var wrapper = Wrappers.lambdaQuery(ShopOrder.class).eq(ShopOrder::getCustomerId, customer.getId());
        if (status != null) {
            wrapper.eq(ShopOrder::getStatus, status);
        }
        wrapper.orderByDesc(ShopOrder::getId);
        Page<ShopOrder> result = shopOrderMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(this::hideUnpaidSource);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public PageResult<ShopOrder> adminPage(long page, long size, Integer status) {
        var wrapper = Wrappers.lambdaQuery(ShopOrder.class);
        if (status != null) {
            wrapper.eq(ShopOrder::getStatus, status);
        }
        wrapper.orderByDesc(ShopOrder::getId);
        Page<ShopOrder> result = shopOrderMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public void cancelIfPending(String orderNo) {
        if (StrUtil.isBlank(orderNo)) {
            return;
        }
        shopOrderMapper.update(null, Wrappers.lambdaUpdate(ShopOrder.class)
                .set(ShopOrder::getStatus, CANCELLED)
                .set(ShopOrder::getUpdateTime, LocalDateTime.now())
                .eq(ShopOrder::getOrderNo, orderNo)
                .eq(ShopOrder::getStatus, PENDING));
    }

    public void adjustPoints(Long customerId, Integer points) {
        AssertUtil.notNull(customerId, "买家不存在");
        AssertUtil.isTrue(points != null && points >= 0, "积分不能小于 0");
        ShopCustomer customer = shopCustomerMapper.selectById(customerId);
        AssertUtil.notNull(customer, "买家不存在");
        customer.setPoints(points);
        customer.setUpdateTime(LocalDateTime.now());
        shopCustomerMapper.updateById(customer);
    }

    public PageResult<ShopCustomer> customers(long page, long size) {
        Page<ShopCustomer> result = shopCustomerMapper.selectPage(new Page<>(page, size),
                Wrappers.lambdaQuery(ShopCustomer.class).orderByDesc(ShopCustomer::getId));
        result.getRecords().forEach(item -> item.setPassword(null));
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    private void deductPoints(Long customerId, int cost) {
        int rows = shopCustomerMapper.update(null, Wrappers.lambdaUpdate(ShopCustomer.class)
                .setSql("points = points - " + cost)
                .eq(ShopCustomer::getId, customerId)
                .ge(ShopCustomer::getPoints, cost));
        AssertUtil.isTrue(rows == 1, "积分不足");
    }

    private boolean channelReady(String payType) {
        if ("ALIPAY".equals(payType)) {
            return StrUtil.isNotBlank(alipayAppId);
        }
        return StrUtil.isNotBlank(wechatMchId);
    }

    private String nextOrderNo() {
        return "A" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);
    }

    private void hideUnpaidSource(ShopOrder order) {
        if (order.getStatus() == null || order.getStatus() != PAID) {
            order.setSourceUrl(null);
        }
    }

    private Map<String, Object> view(ShopOrder order, String tip) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("status", order.getStatus());
        data.put("payType", order.getPayType());
        data.put("amountCent", order.getAmountCent());
        data.put("pointsCost", order.getPointsCost());
        data.put("sourceUrl", order.getStatus() != null && order.getStatus() == PAID ? order.getSourceUrl() : null);
        data.put("tip", tip);
        return data;
    }
}
