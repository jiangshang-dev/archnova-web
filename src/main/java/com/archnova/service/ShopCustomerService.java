package com.archnova.service;

import cn.hutool.core.util.StrUtil;
import com.archnova.domain.base.LoginUser;
import com.archnova.domain.entity.ShopCustomer;
import com.archnova.mapper.ShopCustomerMapper;
import com.archnova.security.TokenService;
import com.archnova.utils.AssertUtil;
import com.archnova.utils.UserUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShopCustomerService {

    private final ShopCustomerMapper shopCustomerMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public Map<String, Object> register(String username, String password, String nickname) {
        AssertUtil.isNotBlank(username, "请输入账号");
        AssertUtil.isNotBlank(password, "请输入密码");
        AssertUtil.isTrue(password.length() >= 6, "密码至少 6 位");
        String name = StrUtil.trim(username);
        Long count = shopCustomerMapper.selectCount(Wrappers.lambdaQuery(ShopCustomer.class).eq(ShopCustomer::getUsername, name));
        AssertUtil.isTrue(count == null || count == 0, "账号已存在");
        ShopCustomer customer = new ShopCustomer();
        customer.setUsername(name);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setNickname(StrUtil.blankToDefault(StrUtil.trim(nickname), name));
        customer.setPoints(0);
        customer.setStatus(1);
        customer.setCreateTime(LocalDateTime.now());
        customer.setUpdateTime(LocalDateTime.now());
        shopCustomerMapper.insert(customer);
        return tokenView(customer);
    }

    public Map<String, Object> login(String username, String password) {
        AssertUtil.isNotBlank(username, "请输入账号");
        AssertUtil.isNotBlank(password, "请输入密码");
        ShopCustomer customer = shopCustomerMapper.selectOne(Wrappers.lambdaQuery(ShopCustomer.class)
                .eq(ShopCustomer::getUsername, StrUtil.trim(username)));
        AssertUtil.notNull(customer, "账号或密码错误");
        AssertUtil.isTrue(customer.getStatus() != null && customer.getStatus() == 1, "账号已停用");
        AssertUtil.isTrue(passwordEncoder.matches(password, customer.getPassword()), "账号或密码错误");
        return tokenView(customer);
    }

    public ShopCustomer current() {
        LoginUser loginUser = UserUtil.getUser();
        AssertUtil.notNull(loginUser, "请先登录");
        AssertUtil.isTrue("CUSTOMER".equals(loginUser.getRole()), "请使用买家账号登录");
        ShopCustomer customer = shopCustomerMapper.selectById(loginUser.getId());
        AssertUtil.notNull(customer, "账号不存在");
        customer.setPassword(null);
        return customer;
    }

    public Map<String, Object> profile() {
        ShopCustomer customer = current();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", customer.getId());
        view.put("username", customer.getUsername());
        view.put("nickname", customer.getNickname());
        view.put("points", customer.getPoints());
        return view;
    }

    private Map<String, Object> tokenView(ShopCustomer customer) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", tokenService.createCustomerToken(customer.getId(), customer.getUsername(), customer.getNickname()));
        result.put("username", customer.getUsername());
        result.put("nickname", customer.getNickname());
        result.put("points", customer.getPoints());
        return result;
    }
}
