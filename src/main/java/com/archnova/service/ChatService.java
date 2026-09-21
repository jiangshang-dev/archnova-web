package com.archnova.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.archnova.domain.entity.ChatMessage;
import com.archnova.domain.entity.ChatSession;
import com.archnova.mapper.ChatMessageMapper;
import com.archnova.mapper.ChatSessionMapper;
import com.archnova.utils.AssertUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    public static final String VISITOR = "visitor";
    public static final String ADMIN = "admin";

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;

    public ChatSession openVisitor(String visitorToken, String clientIp, String account) {
        AssertUtil.isNotBlank(visitorToken, "访客标识不能为空");
        AssertUtil.isTrue(visitorToken.matches("[A-Za-z0-9\\-]{8,64}"), "访客标识不正确");
        ChatSession session = chatSessionMapper.selectOne(
                Wrappers.lambdaQuery(ChatSession.class).eq(ChatSession::getVisitorToken, visitorToken));
        if (session != null) {
            boolean changed = false;
            if (StrUtil.isNotBlank(clientIp) && !clientIp.equals(session.getClientIp())) {
                session.setClientIp(clientIp);
                changed = true;
            }
            String name = displayName(visitorToken, account);
            if (!name.equals(session.getVisitorName())) {
                session.setVisitorName(name);
                changed = true;
            }
            if (changed) {
                session.setUpdateTime(LocalDateTime.now());
                chatSessionMapper.updateById(session);
            }
            return session;
        }
        session = new ChatSession();
        session.setVisitorToken(visitorToken);
        session.setVisitorName(displayName(visitorToken, account));
        session.setClientIp(clientIp);
        session.setLastMessage("");
        session.setUnreadCount(0);
        session.setStatus(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.insert(session);
        return session;
    }

    public ChatMessage send(Long sessionId, String senderType, String content, String clientIp) {
        AssertUtil.notNull(sessionId, "会话不存在");
        AssertUtil.isNotBlank(content, "请输入消息");
        String text = StrUtil.trim(content);
        AssertUtil.isTrue(text.length() <= 1000, "消息不能超过 1000 字");
        ChatSession session = chatSessionMapper.selectById(sessionId);
        AssertUtil.notNull(session, "会话不存在");
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderType(senderType);
        message.setContent(text);
        message.setClientIp(clientIp);
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);
        session.setLastMessage(StrUtil.maxLength(text, 180));
        session.setUpdateTime(LocalDateTime.now());
        session.setStatus(0);
        if (VISITOR.equals(senderType)) {
            session.setUnreadCount((session.getUnreadCount() == null ? 0 : session.getUnreadCount()) + 1);
            if (StrUtil.isNotBlank(clientIp)) {
                session.setClientIp(clientIp);
            }
        }
        chatSessionMapper.updateById(session);
        return message;
    }

    public List<Map<String, Object>> history(Long sessionId, boolean clearUnread) {
        AssertUtil.notNull(sessionId, "会话不存在");
        if (clearUnread) {
            ChatSession session = chatSessionMapper.selectById(sessionId);
            AssertUtil.notNull(session, "会话不存在");
            session.setUnreadCount(0);
            chatSessionMapper.updateById(session);
        }
        List<ChatMessage> messages = chatMessageMapper.selectList(
                Wrappers.lambdaQuery(ChatMessage.class)
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getId)
                        .last("limit 200"));
        return messages.stream().map(this::toView).toList();
    }

    private String displayName(String visitorToken, String account) {
        if (StrUtil.isNotBlank(account)) {
            return account;
        }
        return "访客" + visitorToken.substring(visitorToken.length() - 4);
    }

    public List<ChatSession> sessions() {
        return chatSessionMapper.selectList(Wrappers.lambdaQuery(ChatSession.class)
                .orderByDesc(ChatSession::getUpdateTime)
                .last("limit 200"));
    }

    public ChatSession get(Long id) {
        ChatSession session = chatSessionMapper.selectById(id);
        AssertUtil.notNull(session, "会话不存在");
        return session;
    }

    public ChatSession requireByToken(String visitorToken) {
        ChatSession session = chatSessionMapper.selectOne(
                Wrappers.lambdaQuery(ChatSession.class).eq(ChatSession::getVisitorToken, visitorToken));
        AssertUtil.notNull(session, "会话不存在");
        return session;
    }

    public Map<String, Object> toView(ChatMessage message) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", message.getId());
        view.put("sessionId", message.getSessionId());
        view.put("senderType", message.getSenderType());
        view.put("content", message.getContent());
        view.put("clientIp", message.getClientIp());
        view.put("createTime", message.getCreateTime() == null ? "" : LocalDateTimeUtil.formatNormal(message.getCreateTime()));
        return view;
    }

    public long openCount() {
        Long count = chatSessionMapper.selectCount(Wrappers.lambdaQuery(ChatSession.class).eq(ChatSession::getStatus, 0));
        return count == null ? 0 : count;
    }
}
