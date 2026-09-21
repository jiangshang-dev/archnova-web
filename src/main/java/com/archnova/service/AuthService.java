package com.archnova.service;

import cn.hutool.core.util.StrUtil;
import com.archnova.domain.base.LoginUser;
import com.archnova.domain.entity.SysUser;
import com.archnova.mapper.SysUserMapper;
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
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public Map<String, Object> login(String username, String password) {
        AssertUtil.isNotBlank(username, "请输入账号");
        AssertUtil.isNotBlank(password, "请输入密码");
        SysUser user = sysUserMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, StrUtil.trim(username)));
        AssertUtil.notNull(user, "账号或密码错误");
        AssertUtil.isTrue(user.getStatus() != null && user.getStatus() == 1, "账号已停用");
        AssertUtil.isTrue(passwordEncoder.matches(password, user.getPassword()), "账号或密码错误");
        String token = tokenService.createToken(user);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        return result;
    }

    public LoginUser current() {
        LoginUser loginUser = UserUtil.getUser();
        AssertUtil.notNull(loginUser, "请先登录");
        return loginUser;
    }

    public void changePassword(String oldPassword, String newPassword) {
        LoginUser loginUser = current();
        AssertUtil.isNotBlank(oldPassword, "请输入原密码");
        AssertUtil.isNotBlank(newPassword, "请输入新密码");
        AssertUtil.isTrue(newPassword.length() >= 6, "新密码至少 6 位");
        SysUser user = sysUserMapper.selectById(loginUser.getId());
        AssertUtil.notNull(user, "账号不存在");
        AssertUtil.isTrue(passwordEncoder.matches(oldPassword, user.getPassword()), "原密码不正确");
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }
}
