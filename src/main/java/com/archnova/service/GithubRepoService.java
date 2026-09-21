package com.archnova.service;

import cn.hutool.core.util.StrUtil;
import com.archnova.common.PageResult;
import com.archnova.domain.entity.GithubRepo;
import com.archnova.mapper.GithubRepoMapper;
import com.archnova.utils.AssertUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GithubRepoService {

    private final GithubRepoMapper githubRepoMapper;
    private final GithubService githubService;

    public List<GithubRepo> published() {
        if (githubRepoMapper.selectCount(null) == 0) {
            sync();
        }
        return githubRepoMapper.selectList(Wrappers.lambdaQuery(GithubRepo.class)
                .eq(GithubRepo::getStatus, 1)
                .orderByAsc(GithubRepo::getSortNum)
                .orderByDesc(GithubRepo::getGithubUpdated)
                .orderByDesc(GithubRepo::getId));
    }

    public PageResult<GithubRepo> page(long page, long size, String keyword, Integer status) {
        AssertUtil.isTrue(page > 0, "页码不正确");
        LambdaQueryWrapper<GithubRepo> wrapper = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(item -> item.like(GithubRepo::getName, keyword).or().like(GithubRepo::getDescription, keyword));
        }
        if (status != null) {
            wrapper.eq(GithubRepo::getStatus, status);
        }
        wrapper.orderByAsc(GithubRepo::getSortNum).orderByDesc(GithubRepo::getGithubUpdated).orderByDesc(GithubRepo::getId);
        Page<GithubRepo> result = githubRepoMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public void save(GithubRepo repo) {
        AssertUtil.notNull(repo, "项目不能为空");
        String name = StrUtil.trim(repo.getName());
        AssertUtil.isNotBlank(name, "请填写仓库名");
        AssertUtil.isNotBlank(repo.getHtmlUrl(), "请填写 GitHub 地址");
        repo.setName(name);
        repo.setDescription(StrUtil.blankToDefault(StrUtil.trim(repo.getDescription()), "暂无简介"));
        repo.setLanguage(StrUtil.blankToDefault(StrUtil.trim(repo.getLanguage()), "其他"));
        if (repo.getStars() == null) {
            repo.setStars(0);
        }
        if (repo.getSortNum() == null) {
            repo.setSortNum(0);
        }
        if (repo.getStatus() == null) {
            repo.setStatus(1);
        }
        Long sameName = githubRepoMapper.selectCount(Wrappers.lambdaQuery(GithubRepo.class)
                .eq(GithubRepo::getName, name)
                .ne(repo.getId() != null, GithubRepo::getId, repo.getId()));
        AssertUtil.isTrue(sameName == null || sameName == 0, "仓库名已存在");
        repo.setUpdateTime(LocalDateTime.now());
        if (repo.getId() == null) {
            repo.setCreateTime(LocalDateTime.now());
            githubRepoMapper.insert(repo);
            return;
        }
        AssertUtil.notNull(githubRepoMapper.selectById(repo.getId()), "项目不存在");
        githubRepoMapper.updateById(repo);
    }

    public synchronized Map<String, Integer> sync() {
        boolean showNew = githubRepoMapper.selectCount(null) == 0;
        int added = 0;
        int updated = 0;
        for (Map<String, Object> remote : githubService.repos()) {
            String name = String.valueOf(remote.get("name"));
            GithubRepo existing = githubRepoMapper.selectOne(Wrappers.lambdaQuery(GithubRepo.class).eq(GithubRepo::getName, name));
            if (existing == null) {
                GithubRepo repo = new GithubRepo();
                repo.setName(name);
                repo.setDescription(String.valueOf(remote.get("description")));
                repo.setLanguage(String.valueOf(remote.get("language")));
                repo.setHtmlUrl(String.valueOf(remote.get("htmlUrl")));
                repo.setHomepage(remote.get("homepage") == null ? null : String.valueOf(remote.get("homepage")));
                repo.setStars(toInt(remote.get("stars")));
                repo.setGithubUpdated(String.valueOf(remote.get("updatedAt")));
                repo.setSortNum(0);
                repo.setStatus(showNew ? 1 : 0);
                repo.setCreateTime(LocalDateTime.now());
                repo.setUpdateTime(LocalDateTime.now());
                githubRepoMapper.insert(repo);
                added++;
                continue;
            }
            existing.setLanguage(String.valueOf(remote.get("language")));
            existing.setHtmlUrl(String.valueOf(remote.get("htmlUrl")));
            existing.setHomepage(remote.get("homepage") == null ? null : String.valueOf(remote.get("homepage")));
            existing.setStars(toInt(remote.get("stars")));
            existing.setGithubUpdated(String.valueOf(remote.get("updatedAt")));
            existing.setUpdateTime(LocalDateTime.now());
            githubRepoMapper.updateById(existing);
            updated++;
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("added", added);
        result.put("updated", updated);
        result.put("showNew", showNew ? 1 : 0);
        return result;
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }
}
