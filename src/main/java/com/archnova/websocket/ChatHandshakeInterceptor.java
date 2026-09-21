package com.archnova.websocket;

import cn.hutool.core.util.StrUtil;
import com.archnova.security.TokenService;
import com.archnova.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenService tokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        attributes.put("ip", IpUtils.getIpAddr(httpRequest));
        attributes.put("role", StrUtil.blankToDefault(httpRequest.getParameter("role"), "visitor"));
        attributes.put("visitorToken", httpRequest.getParameter("visitorToken"));
        String shopToken = httpRequest.getParameter("shopToken");
        if (StrUtil.isNotBlank(shopToken)) {
            var customer = tokenService.parse(shopToken);
            if (customer != null && "CUSTOMER".equals(customer.getRole())) {
                attributes.put("customerName", customer.getUsername());
            }
        }
        String token = httpRequest.getParameter("token");
        if (StrUtil.isNotBlank(token)) {
            attributes.put("loginUser", tokenService.parse(token));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        // 无需处理
    }
}
