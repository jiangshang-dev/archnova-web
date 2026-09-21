package com.archnova.domain.base;

import lombok.Data;

/**
 * 当前登录用户
 */
@Data
public class LoginUser {

    private Long id;

    private String username;

    private String realName;

    /**
     * ADMIN 或 CUSTOMER
     */
    private String role;

    private String token;
}
