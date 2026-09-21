package com.archnova.utils;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 客户端 IP
 */
@Slf4j
public class IpUtils {

    private static final String UNKNOWN = "unknown";

    private IpUtils() {
    }

    /**
     * 获取客户端 IP。经过反向代理时取 X-Forwarded-For 中第一个有效地址。
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = null;
        try {
            ip = firstValid(
                    request.getHeader("x-forwarded-for"),
                    request.getHeader("X-Real-IP"),
                    request.getHeader("Proxy-Client-IP"),
                    request.getHeader("WL-Proxy-Client-IP"),
                    request.getHeader("HTTP_CLIENT_IP"),
                    request.getHeader("HTTP_X_FORWARDED_FOR"),
                    request.getRemoteAddr()
            );
        } catch (Exception e) {
            log.error("获取客户端 IP 失败", e);
        }
        if (StrUtil.isBlank(ip)) {
            return "";
        }
        if (ip.contains(",")) {
            for (String part : ip.split(",")) {
                String candidate = StrUtil.trim(part);
                if (isValidIpAddress(candidate)) {
                    return normalize(candidate);
                }
            }
        }
        return normalize(ip);
    }

    public static boolean isValidIpAddress(String ipAddress) {
        return Validator.isIpv4(ipAddress) || Validator.isIpv6(ipAddress);
    }

    public static String getServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("获取服务器 IP 失败", e);
            return "";
        }
    }

    private static String firstValid(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (StrUtil.isNotBlank(candidate) && !UNKNOWN.equalsIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static String normalize(String ip) {
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}
