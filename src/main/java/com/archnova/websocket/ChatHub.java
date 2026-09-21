package com.archnova.websocket;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONUtil;
import com.archnova.domain.entity.ChatSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHub {

    private final Map<String, WebSocketSession> visitors = new ConcurrentHashMap<>();
    private final Set<WebSocketSession> admins = ConcurrentHashMap.newKeySet();

    public void bindVisitor(String visitorToken, WebSocketSession session) {
        WebSocketSession previous = visitors.put(visitorToken, session);
        closeQuietly(previous, session);
    }

    public void bindAdmin(WebSocketSession session) {
        admins.add(session);
    }

    public void unbind(WebSocketSession session) {
        visitors.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
        admins.remove(session);
    }

    public void pushVisitor(String visitorToken, Map<String, Object> payload) {
        send(visitors.get(visitorToken), payload);
    }

    public void pushAdmins(Map<String, Object> payload) {
        for (WebSocketSession session : admins) {
            send(session, payload);
        }
    }

    public void send(WebSocketSession session, Map<String, Object> payload) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(payload)));
            }
        } catch (IOException ignored) {
            unbind(session);
        }
    }

    public List<Map<String, Object>> sessionViews(List<ChatSession> sessions) {
        return sessions.stream().map(session -> {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", session.getId());
            view.put("visitorName", session.getVisitorName());
            view.put("clientIp", session.getClientIp());
            view.put("lastMessage", session.getLastMessage());
            view.put("unreadCount", session.getUnreadCount());
            view.put("updateTime", session.getUpdateTime() == null ? "" : LocalDateTimeUtil.formatNormal(session.getUpdateTime()));
            return view;
        }).toList();
    }

    private void closeQuietly(WebSocketSession previous, WebSocketSession current) {
        if (previous == null || previous.getId().equals(current.getId()) || !previous.isOpen()) {
            return;
        }
        try {
            previous.close();
        } catch (IOException ignored) {
            // 旧连接关闭失败不影响新连接
        }
    }
}
