package com.archnova.controller.shop;

import com.archnova.common.PageResult;
import com.archnova.common.R;
import com.archnova.domain.entity.ShopOrder;
import com.archnova.service.ShopCustomerService;
import com.archnova.service.ShopOrderService;
import com.archnova.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopCustomerService shopCustomerService;
    private final ShopOrderService shopOrderService;

    @PostMapping("/register")
    public R<Map<String, Object>> register(@RequestBody AuthRequest request) {
        return R.ok(shopCustomerService.register(request.getUsername(), request.getPassword(), request.getNickname()));
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody AuthRequest request) {
        return R.ok(shopCustomerService.login(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(shopCustomerService.profile());
    }

    @PostMapping("/orders")
    public R<Map<String, Object>> createOrder(@RequestBody OrderRequest request, HttpServletRequest httpRequest) {
        return R.ok(shopOrderService.create(request.getProductId(), request.getPayType(), IpUtils.getIpAddr(httpRequest)));
    }

    @GetMapping("/orders")
    public R<PageResult<ShopOrder>> orders(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) Integer status) {
        return R.ok(shopOrderService.myOrders(page, size, status));
    }

    @Data
    public static class AuthRequest {
        private String username;
        private String password;
        private String nickname;
    }

    @Data
    public static class OrderRequest {
        private Long productId;
        private String payType;
    }
}
