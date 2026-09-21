package com.archnova.security;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import com.archnova.domain.base.LoginUser;
import com.archnova.domain.entity.SysUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    @Value("${archnova.jwt-secret}")
    private String secret;

    @Value("${archnova.jwt-expire-hours:168}")
    private int expireHours;

    public String createToken(SysUser user) {
        return JWT.create()
                .setPayload("uid", user.getId())
                .setPayload("username", user.getUsername())
                .setPayload("realName", StrUtil.blankToDefault(user.getRealName(), user.getUsername()))
                .setExpiresAt(DateUtil.offsetHour(new Date(), expireHours))
                .setKey(secretBytes())
                .sign();
    }

    public LoginUser parse(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            JWT jwt = JWT.of(token).setKey(secretBytes());
            if (!jwt.verify()) {
                return null;
            }
            LoginUser loginUser = new LoginUser();
            loginUser.setId(Convert.toLong(jwt.getPayload("uid")));
            loginUser.setUsername(Convert.toStr(jwt.getPayload("username")));
            loginUser.setRealName(Convert.toStr(jwt.getPayload("realName")));
            loginUser.setToken(token);
            return loginUser;
        } catch (Exception ignored) {
            return null;
        }
    }

    private byte[] secretBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
