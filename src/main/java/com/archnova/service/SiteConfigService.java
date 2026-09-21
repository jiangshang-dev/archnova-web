package com.archnova.service;

import cn.hutool.core.util.StrUtil;
import com.archnova.domain.entity.SiteConfig;
import com.archnova.mapper.SiteConfigMapper;
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
public class SiteConfigService {

    public static final List<String> KEYS = List.of(
            "company_name",
            "slogan",
            "location",
            "email",
            "wechat_id",
            "wechat_qr",
            "github_url",
            "github_user",
            "phone"
    );

    private final SiteConfigMapper siteConfigMapper;

    public Map<String, String> publicConfig() {
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : KEYS) {
            result.put(key, "");
        }
        List<SiteConfig> list = siteConfigMapper.selectList(Wrappers.lambdaQuery(SiteConfig.class));
        for (SiteConfig item : list) {
            if (KEYS.contains(item.getConfigKey())) {
                result.put(item.getConfigKey(), StrUtil.nullToEmpty(item.getConfigValue()));
            }
        }
        return result;
    }

    public void update(Map<String, String> values) {
        AssertUtil.notNull(values, "配置不能为空");
        for (String key : KEYS) {
            if (!values.containsKey(key)) {
                continue;
            }
            String value = StrUtil.nullToEmpty(values.get(key));
            SiteConfig existing = siteConfigMapper.selectOne(Wrappers.lambdaQuery(SiteConfig.class).eq(SiteConfig::getConfigKey, key));
            if (existing == null) {
                SiteConfig created = new SiteConfig();
                created.setConfigKey(key);
                created.setConfigValue(value);
                created.setUpdateTime(LocalDateTime.now());
                siteConfigMapper.insert(created);
            } else {
                existing.setConfigValue(value);
                existing.setUpdateTime(LocalDateTime.now());
                siteConfigMapper.updateById(existing);
            }
        }
    }

    public String getValue(String key) {
        SiteConfig config = siteConfigMapper.selectOne(Wrappers.lambdaQuery(SiteConfig.class).eq(SiteConfig::getConfigKey, key));
        return config == null ? "" : StrUtil.nullToEmpty(config.getConfigValue());
    }
}
