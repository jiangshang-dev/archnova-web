package com.archnova.mapper;

import com.archnova.domain.entity.GithubRepo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GithubRepoMapper extends BaseMapper<GithubRepo> {
}
