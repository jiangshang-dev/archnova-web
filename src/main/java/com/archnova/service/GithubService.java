package com.archnova.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubService {

    private final SiteConfigService siteConfigService;

    private volatile List<Map<String, Object>> cache = List.of();
    private volatile long cacheAt;
    private volatile String cacheUser = "";

    public List<Map<String, Object>> repos() {
        String user = StrUtil.blankToDefault(siteConfigService.getValue("github_user"), "jiangshang-dev");
        long now = System.currentTimeMillis();
        if (user.equals(cacheUser) && now - cacheAt < 10 * 60 * 1000 && !cache.isEmpty()) {
            return cache;
        }
        try {
            String body = HttpRequest.get("https://api.github.com/users/" + user + "/repos?per_page=100&sort=updated")
                    .header("User-Agent", "archnova-web")
                    .header("Accept", "application/vnd.github+json")
                    .timeout(8000)
                    .execute()
                    .body();
            if (!JSONUtil.isTypeJSONArray(body)) {
                return cache;
            }
            JSONArray array = JSONUtil.parseArray(body);
            List<Map<String, Object>> repos = new ArrayList<>();
            for (Object item : array) {
                JSONObject repo = (JSONObject) item;
                if (Boolean.TRUE.equals(repo.getBool("fork"))) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", repo.getStr("name"));
                row.put("description", StrUtil.blankToDefault(repo.getStr("description"), "暂无简介"));
                row.put("language", StrUtil.blankToDefault(repo.getStr("language"), "其他"));
                row.put("htmlUrl", repo.getStr("html_url"));
                row.put("homepage", repo.getStr("homepage"));
                row.put("stars", repo.getInt("stargazers_count", 0));
                row.put("updatedAt", StrUtil.sub(repo.getStr("updated_at"), 0, 10));
                repos.add(row);
            }
            repos.sort(Comparator.comparing((Map<String, Object> item) -> String.valueOf(item.get("updatedAt"))).reversed());
            cache = repos;
            cacheAt = now;
            cacheUser = user;
            return repos;
        } catch (Exception e) {
            log.warn("拉取 GitHub 仓库失败: {}", e.getMessage());
            return cache;
        }
    }
}
