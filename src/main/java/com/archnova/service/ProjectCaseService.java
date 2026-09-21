package com.archnova.service;

import cn.hutool.core.util.StrUtil;
import com.archnova.common.PageResult;
import com.archnova.domain.entity.ProjectCase;
import com.archnova.mapper.ProjectCaseMapper;
import com.archnova.utils.AssertUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectCaseService {

    private final ProjectCaseMapper projectCaseMapper;

    public List<ProjectCase> published(String category) {
        LambdaQueryWrapper<ProjectCase> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ProjectCase::getStatus, 1);
        if (StrUtil.isNotBlank(category) && !"全部".equals(category)) {
            wrapper.eq(ProjectCase::getCategory, category);
        }
        wrapper.orderByAsc(ProjectCase::getSortNum).orderByDesc(ProjectCase::getId);
        return projectCaseMapper.selectList(wrapper);
    }

    public ProjectCase publishedDetail(Long id) {
        ProjectCase projectCase = projectCaseMapper.selectById(id);
        AssertUtil.notNull(projectCase, "案例不存在");
        AssertUtil.isTrue(projectCase.getStatus() != null && projectCase.getStatus() == 1, "案例未上架");
        return projectCase;
    }

    public PageResult<ProjectCase> page(long page, long size, String keyword, Integer status) {
        AssertUtil.isTrue(page > 0, "页码不正确");
        AssertUtil.isTrue(size > 0 && size <= 100, "每页数量不正确");
        LambdaQueryWrapper<ProjectCase> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(ProjectCase::getTitle, keyword).or().like(ProjectCase::getSummary, keyword));
        }
        if (status != null) {
            wrapper.eq(ProjectCase::getStatus, status);
        }
        wrapper.orderByAsc(ProjectCase::getSortNum).orderByDesc(ProjectCase::getId);
        Page<ProjectCase> result = projectCaseMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public void save(ProjectCase projectCase) {
        AssertUtil.notNull(projectCase, "案例不能为空");
        AssertUtil.isNotBlank(projectCase.getTitle(), "请填写案例标题");
        AssertUtil.isNotBlank(projectCase.getSummary(), "请填写案例摘要");
        AssertUtil.isNotBlank(projectCase.getCategory(), "请选择业务类型");
        if (projectCase.getSortNum() == null) {
            projectCase.setSortNum(0);
        }
        if (projectCase.getStatus() == null) {
            projectCase.setStatus(1);
        }
        projectCase.setUpdateTime(LocalDateTime.now());
        if (projectCase.getId() == null) {
            projectCase.setDeleted(0);
            projectCase.setCreateTime(LocalDateTime.now());
            projectCaseMapper.insert(projectCase);
            return;
        }
        ProjectCase existing = projectCaseMapper.selectById(projectCase.getId());
        AssertUtil.notNull(existing, "案例不存在");
        projectCaseMapper.updateById(projectCase);
    }

    public void delete(Long id) {
        AssertUtil.notNull(id, "案例不存在");
        AssertUtil.isTrue(projectCaseMapper.deleteById(id) > 0, "案例不存在");
    }
}
