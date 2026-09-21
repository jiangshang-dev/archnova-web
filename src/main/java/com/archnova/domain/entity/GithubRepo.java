package com.archnova.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("github_repo")
public class GithubRepo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String language;

    private String htmlUrl;

    private String homepage;

    private Integer stars;

    private String githubUpdated;

    private Integer sortNum;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
