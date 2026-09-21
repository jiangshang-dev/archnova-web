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
    private final MailCodeService mailCodeService;

    public Map<String, Object> register(String email, String code, String username, String password) {
        String address = mailCodeService.normalize(email);
        String name = normalizeUsername(username);
        AssertUtil.isNotBlank(password, "请输入密码");
        AssertUtil.isTrue(password.length() >= 6, "密码至少 6 位");
        Long nameCount = shopCustomerMapper.selectCount(Wrappers.lambdaQuery(ShopCustomer.class).eq(ShopCustomer::getUsername, name));
        AssertUtil.isTrue(nameCount == null || nameCount == 0, "用户名已存在");
        Long emailCount = shopCustomerMapper.selectCount(Wrappers.lambdaQuery(ShopCustomer.class).eq(ShopCustomer::getEmail, address));
        AssertUtil.isTrue(emailCount == null || emailCount == 0, "邮箱已注册");
        mailCodeService.verify(address, "register", code);
        ShopCustomer customer = new ShopCustomer();
        customer.setUsername(name);
        customer.setEmail(address);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setNickname(name);
        customer.setPoints(0);
        customer.setStatus(1);
        customer.setCreateTime(LocalDateTime.now());
        customer.setUpdateTime(LocalDateTime.now());
        shopCustomerMapper.insert(customer);
        return tokenView(customer);
    }

    public Map<String, Object> login(String account, String password) {
        AssertUtil.isNotBlank(account, "请输入账号或邮箱");
        AssertUtil.isNotBlank(password, "请输入密码");
        String value = StrUtil.trim(account);
        ShopCustomer customer = value.contains("@")
                ? shopCustomerMapper.selectOne(Wrappers.lambdaQuery(ShopCustomer.class).eq(ShopCustomer::getEmail, value.toLowerCase()))
                : shopCustomerMapper.selectOne(Wrappers.lambdaQuery(ShopCustomer.class).eq(ShopCustomer::getUsername, value));
        AssertUtil.notNull(customer, "账号或密码错误");
        AssertUtil.isTrue(customer.getStatus() != null && customer.getStatus() == 1, "账号已停用");
        AssertUtil.isTrue(passwordEncoder.matches(password, customer.getPassword()), "账号或密码错误");
        return tokenView(customer);
    }

    public void sendRegisterCode(String email) {
        String address = mailCodeService.normalize(email);
        Long count = shopCustomerMapper.selectCount(Wrappers.lambdaQuery(ShopCustomer.class).eq(ShopCustomer::getEmail, address));
        AssertUtil.isTrue(count == null || count == 0, "邮箱已注册");
        mailCodeService.send(address, "register");
    }

    public void sendEmailCode(String email) {
        String address = mailCodeService.normalize(email);
        ShopCustomer me = current();
        AssertUtil.notTrue(address.equalsIgnoreCase(StrUtil.blankToDefault(me.getEmail(), "")), "这已经是当前邮箱");
        Long count = shopCustomerMapper.selectCount(Wrappers.lambdaQuery(ShopCustomer.class)
                .eq(ShopCustomer::getEmail, address)
                .ne(ShopCustomer::getId, me.getId()));
        AssertUtil.isTrue(count == null || count == 0, "邮箱已被占用");
        mailCodeService.send(address, "bind:" + me.getId());
    }

    public Map<String, Object> updateNickname(String nickname) {
        String name = StrUtil.trim(nickname);
        AssertUtil.isNotBlank(name, "请输入昵称");
        AssertUtil.isTrue(name.length() <= 32, "昵称不能超过 32 个字");
        ShopCustomer customer = requireWritable();
        customer.setNickname(name);
        customer.setUpdateTime(LocalDateTime.now());
        shopCustomerMapper.updateById(customer);
        return profile();
    }

    public Map<String, Object> updateEmail(String email, String code) {
        ShopCustomer customer = requireWritable();
        String address = mailCodeService.normalize(email);
        mailCodeService.verify(address, "bind:" + customer.getId(), code);
        Long count = shopCustomerMapper.selectCount(Wrappers.lambdaQuery(ShopCustomer.class)
                .eq(ShopCustomer::getEmail, address)
                .ne(ShopCustomer::getId, customer.getId()));
        AssertUtil.isTrue(count == null || count == 0, "邮箱已被占用");
        customer.setEmail(address);
        customer.setUpdateTime(LocalDateTime.now());
        shopCustomerMapper.updateById(customer);
        return profile();
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
        view.put("email", customer.getEmail());
        view.put("points", customer.getPoints());
        return view;
    }

    private ShopCustomer requireWritable() {
        ShopCustomer customer = current();
        ShopCustomer stored = shopCustomerMapper.selectById(customer.getId());
        AssertUtil.notNull(stored, "账号不存在");
        return stored;
    }

    private String normalizeUsername(String username) {
        String name = StrUtil.trim(username);
        AssertUtil.isTrue(name.matches("[A-Za-z0-9_\\u4e00-\\u9fa5]{2,32}"), "用户名请用 2 到 32 位字母、数字或中文");
        return name;
    }

    private Map<String, Object> tokenView(ShopCustomer customer) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", tokenService.createCustomerToken(customer.getId(), customer.getUsername(), customer.getNickname()));
        result.put("username", customer.getUsername());
        result.put("nickname", customer.getNickname());
        result.put("email", customer.getEmail());
        result.put("points", customer.getPoints());
        return result;
    }
}
