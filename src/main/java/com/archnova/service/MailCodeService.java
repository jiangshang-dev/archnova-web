package com.archnova.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.lang.Validator;
import com.archnova.utils.AssertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MailCodeService {

    private static final long EXPIRE_MS = 10 * 60 * 1000L;
    private static final long INTERVAL_MS = 60 * 1000L;

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ConcurrentHashMap<String, Pending> codes = new ConcurrentHashMap<>();

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public void send(String email, String scene) {
        String address = normalize(email);
        String key = scene + ":" + address;
        long now = System.currentTimeMillis();
        Pending current = codes.get(key);
        if (current != null) {
            AssertUtil.isTrue(now >= current.nextSendAt, "验证码已发送，请稍后再试");
        }
        String code = RandomUtil.randomNumbers(6);
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        AssertUtil.isTrue(sender != null && StrUtil.isNotBlank(mailHost) && StrUtil.isNotBlank(mailFrom),
                "邮件服务未配置，暂时不能发送验证码");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(address);
        message.setSubject("极构科技验证码");
        message.setText("验证码是 " + code + "，10 分钟内有效。如非本人操作，请忽略这封邮件。");
        sender.send(message);
        codes.put(key, new Pending(code, now + EXPIRE_MS, now + INTERVAL_MS));
    }

    public void verify(String email, String scene, String code) {
        String address = normalize(email);
        AssertUtil.isNotBlank(code, "请输入验证码");
        Pending pending = codes.get(scene + ":" + address);
        long now = System.currentTimeMillis();
        AssertUtil.isTrue(pending != null && now <= pending.expireAt && pending.code.equals(StrUtil.trim(code)),
                "验证码不正确或已过期");
        codes.remove(scene + ":" + address);
    }

    public String normalize(String email) {
        String address = StrUtil.trim(email).toLowerCase();
        AssertUtil.isTrue(Validator.isEmail(address), "请输入正确的邮箱");
        return address;
    }

    private record Pending(String code, long expireAt, long nextSendAt) {
    }
}
