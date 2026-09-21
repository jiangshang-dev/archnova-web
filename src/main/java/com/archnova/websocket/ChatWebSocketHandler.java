package com.archnova.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.archnova.domain.base.LoginUser;
import com.archnova.domain.entity.ChatMessage;
import com.archnova.domain.entity.ChatSession;
import com.archnova.exception.ServiceException;
import com.archnova.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ChatHub chatHub;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String role = String.valueOf(session.getAttributes().get("role"));
        if ("admin".equals(role)) {
            LoginUser loginUser = (LoginUser) session.getAttributes().get("loginUser");
            if (loginUser == null) {
                chatHub.send(session, error("请先登录"));
                close(session);
                return;
            }
            chatHub.bindAdmin(session);
            pushSessions();
            return;
        }
        String visitorToken = StrUtil.toStringOrNull(session.getAttributes().get("visitorToken"));
        try {
            ChatSession chatSession = chatService.openVisitor(visitorToken, ip(session));
            session.getAttributes().put("visitorToken", chatSession.getVisitorToken());
            session.getAttributes().put("sessionId", chatSession.getId());
            chatHub.bindVisitor(chatSession.getVisitorToken(), session);
            Map<String, Object> ready = new LinkedHashMap<>();
            ready.put("type", "ready");
            ready.put("sessionId", chatSession.getId());
            ready.put("clientIp", chatSession.getClientIp());
            ready.put("messages", chatService.history(chatSession.getId(), false));
            chatHub.send(session, ready);
            pushSessions();
        } catch (ServiceException ex) {
            chatHub.send(session, error(ex.getMessage()));
            close(session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        JSONObject body;
        try {
            body = JSONUtil.parseObj(message.getPayload());
        } catch (Exception ex) {
            chatHub.send(session, error("消息格式不正确"));
            return;
        }
        String action = body.getStr("action");
        try {
            if ("admin".equals(String.valueOf(session.getAttributes().get("role")))) {
                handleAdmin(session, action, body);
                return;
            }
            handleVisitor(session, action, body);
        } catch (ServiceException ex) {
            chatHub.send(session, error(ex.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        chatHub.unbind(session);
    }

    private void handleVisitor(WebSocketSession session, String action, JSONObject body) {
        if (!"send".equals(action)) {
            return;
        }
        Long sessionId = (Long) session.getAttributes().get("sessionId");
        ChatMessage saved = chatService.send(sessionId, ChatService.VISITOR, body.getStr("content"), ip(session));
        Map<String, Object> payload = messagePayload(saved);
        chatHub.pushVisitor(String.valueOf(session.getAttributes().get("visitorToken")), payload);
        chatHub.pushAdmins(payload);
        pushSessions();
    }

    private void handleAdmin(WebSocketSession session, String action, JSONObject body) {
        if ("history".equals(action)) {
            Long sessionId = body.getLong("sessionId");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "history");
            payload.put("sessionId", sessionId);
            payload.put("messages", chatService.history(sessionId, true));
            chatHub.send(session, payload);
            pushSessions();
            return;
        }
        if ("send".equals(action)) {
            ChatMessage saved = chatService.send(body.getLong("sessionId"), ChatService.ADMIN, body.getStr("content"), ip(session));
            Map<String, Object> payload = messagePayload(saved);
            ChatSession chatSession = chatService.get(saved.getSessionId());
            chatHub.pushVisitor(chatSession.getVisitorToken(), payload);
            chatHub.pushAdmins(payload);
            pushSessions();
        }
    }

    private void pushSessions() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "sessions");
        payload.put("sessions", chatHub.sessionViews(chatService.sessions()));
        chatHub.pushAdmins(payload);
    }

    private Map<String, Object> messagePayload(ChatMessage message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "message");
        payload.put("message", chatService.toView(message));
        return payload;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "error");
        payload.put("message", message);
        return payload;
    }

    private String ip(WebSocketSession session) {
        Object ip = session.getAttributes().get("ip");
        return ip == null ? "" : String.valueOf(ip);
    }

    private void close(WebSocketSession session) {
        try {
            session.close();
        } catch (Exception ignored) {
            // 关闭失败忽略
        }
    }
}
