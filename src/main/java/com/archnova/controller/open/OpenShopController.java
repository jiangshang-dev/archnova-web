package com.archnova.controller.open;

import com.archnova.common.R;
import com.archnova.domain.entity.ShopProduct;
import com.archnova.service.ShopProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/open/shop")
@RequiredArgsConstructor
public class OpenShopController {

    private final ShopProductService shopProductService;

    @GetMapping("/products")
    public R<List<ShopProduct>> products() {
        return R.ok(shopProductService.published());
    }

    @GetMapping("/products/{id}")
    public R<ShopProduct> detail(@PathVariable Long id) {
        return R.ok(shopProductService.publishedDetail(id));
    }
}
