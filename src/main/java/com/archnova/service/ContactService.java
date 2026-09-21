package com.archnova.service;

import cn.hutool.core.util.StrUtil;
import com.archnova.common.PageResult;
import com.archnova.domain.entity.ContactMessage;
import com.archnova.mapper.ContactMessageMapper;
import com.archnova.utils.AssertUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageMapper contactMessageMapper;

    public void submit(ContactMessage message, String clientIp) {
        AssertUtil.notNull(message, "请填写联系信息");
        AssertUtil.isNotBlank(message.getName(), "请填写称呼");
        AssertUtil.isNotBlank(message.getContent(), "请填写需求说明");
        AssertUtil.isTrue(StrUtil.isNotBlank(message.getMobile()) || StrUtil.isNotBlank(message.getEmail()), "请留下微信、手机或邮箱");
        AssertUtil.isTrue(message.getContent().length() <= 2000, "需求说明请控制在 2000 字以内");
        message.setId(null);
        message.setName(StrUtil.trim(message.getName()));
        message.setMobile(StrUtil.trim(message.getMobile()));
        message.setEmail(StrUtil.trim(message.getEmail()));
        message.setClientIp(clientIp);
        message.setStatus(0);
        message.setCreateTime(LocalDateTime.now());
        contactMessageMapper.insert(message);
    }

    public PageResult<ContactMessage> page(long page, long size, Integer status) {
        AssertUtil.isTrue(page > 0, "页码不正确");
        var wrapper = Wrappers.lambdaQuery(ContactMessage.class);
        if (status != null) {
            wrapper.eq(ContactMessage::getStatus, status);
        }
        wrapper.orderByDesc(ContactMessage::getId);
        Page<ContactMessage> result = contactMessageMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public void markRead(Long id) {
        ContactMessage message = contactMessageMapper.selectById(id);
        AssertUtil.notNull(message, "留言不存在");
        message.setStatus(1);
        contactMessageMapper.updateById(message);
    }

    public long unreadCount() {
        Long count = contactMessageMapper.selectCount(Wrappers.lambdaQuery(ContactMessage.class).eq(ContactMessage::getStatus, 0));
        return count == null ? 0 : count;
    }
}
