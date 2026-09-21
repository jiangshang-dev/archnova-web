package com.archnova.service;

import cn.hutool.core.util.StrUtil;
import com.archnova.common.PageResult;
import com.archnova.domain.entity.ShopProduct;
import com.archnova.mapper.ShopProductMapper;
import com.archnova.utils.AssertUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopProductService {

    private final ShopProductMapper shopProductMapper;

    public List<ShopProduct> published() {
        List<ShopProduct> list = shopProductMapper.selectList(Wrappers.lambdaQuery(ShopProduct.class)
                .eq(ShopProduct::getStatus, 1)
                .orderByAsc(ShopProduct::getSortNum)
                .orderByDesc(ShopProduct::getId));
        list.forEach(this::hideSource);
        return list;
    }

    public ShopProduct publishedDetail(Long id) {
        ShopProduct product = shopProductMapper.selectById(id);
        AssertUtil.notNull(product, "商品不存在");
        AssertUtil.isTrue(product.getStatus() != null && product.getStatus() == 1, "商品已下架");
        hideSource(product);
        return product;
    }

    public ShopProduct requireOnSale(Long id) {
        ShopProduct product = shopProductMapper.selectById(id);
        AssertUtil.notNull(product, "商品不存在");
        AssertUtil.isTrue(product.getStatus() != null && product.getStatus() == 1, "商品已下架");
        return product;
    }

    public PageResult<ShopProduct> page(long page, long size, String keyword) {
        AssertUtil.isTrue(page > 0, "页码不正确");
        LambdaQueryWrapper<ShopProduct> wrapper = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(ShopProduct::getTitle, keyword);
        }
        wrapper.orderByAsc(ShopProduct::getSortNum).orderByDesc(ShopProduct::getId);
        Page<ShopProduct> result = shopProductMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public void save(ShopProduct product) {
        AssertUtil.notNull(product, "商品不能为空");
        AssertUtil.isNotBlank(product.getTitle(), "请填写商品标题");
        AssertUtil.isNotBlank(product.getSummary(), "请填写摘要");
        AssertUtil.isNotBlank(product.getContentMd(), "请填写商品详情");
        AssertUtil.isTrue(product.getPriceCent() != null && product.getPriceCent() >= 0, "请填写价格");
        AssertUtil.isTrue(product.getPointsPrice() != null && product.getPointsPrice() >= 0, "请填写积分价格");
        if (product.getSortNum() == null) {
            product.setSortNum(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        product.setUpdateTime(LocalDateTime.now());
        if (product.getId() == null) {
            product.setDeleted(0);
            product.setCreateTime(LocalDateTime.now());
            shopProductMapper.insert(product);
            return;
        }
        AssertUtil.notNull(shopProductMapper.selectById(product.getId()), "商品不存在");
        shopProductMapper.updateById(product);
    }

    public void delete(Long id) {
        AssertUtil.isTrue(shopProductMapper.deleteById(id) > 0, "商品不存在");
    }

    private void hideSource(ShopProduct product) {
        product.setSourceUrl(null);
    }
}
