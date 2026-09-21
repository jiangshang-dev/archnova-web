package com.archnova.controller.admin;

import com.archnova.common.PageResult;
import com.archnova.common.R;
import com.archnova.domain.entity.ShopCustomer;
import com.archnova.domain.entity.ShopOrder;
import com.archnova.domain.entity.ShopProduct;
import com.archnova.service.ShopOrderService;
import com.archnova.service.ShopProductService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/shop")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopProductService shopProductService;
    private final ShopOrderService shopOrderService;

    @GetMapping("/products")
    public R<PageResult<ShopProduct>> products(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) String keyword) {
        return R.ok(shopProductService.page(page, size, keyword));
    }

    @PostMapping("/products")
    public R<Void> saveProduct(@RequestBody ShopProduct product) {
        shopProductService.save(product);
        return R.ok();
    }

    @PostMapping("/products/{id}/delete")
    public R<Void> deleteProduct(@PathVariable Long id) {
        shopProductService.delete(id);
        return R.ok();
    }

    @GetMapping("/orders")
    public R<PageResult<ShopOrder>> orders(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) Integer status) {
        return R.ok(shopOrderService.adminPage(page, size, status));
    }

    @GetMapping("/customers")
    public R<PageResult<ShopCustomer>> customers(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "20") long size) {
        return R.ok(shopOrderService.customers(page, size));
    }

    @PostMapping("/customers/{id}/points")
    public R<Void> points(@PathVariable Long id, @RequestBody PointsRequest request) {
        shopOrderService.adjustPoints(id, request.getPoints());
        return R.ok();
    }

    @Data
    public static class PointsRequest {
        private Integer points;
    }
}
