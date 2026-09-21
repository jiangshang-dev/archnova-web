package com.archnova.controller.admin;

import com.archnova.common.PageResult;
import com.archnova.common.R;
import com.archnova.domain.entity.GithubRepo;
import com.archnova.service.GithubRepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/repos")
@RequiredArgsConstructor
public class AdminRepoController {

    private final GithubRepoService githubRepoService;

    @GetMapping
    public R<PageResult<GithubRepo>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status) {
        return R.ok(githubRepoService.page(page, size, keyword, status));
    }

    @PostMapping
    public R<Void> save(@RequestBody GithubRepo repo) {
        githubRepoService.save(repo);
        return R.ok();
    }

    @PostMapping("/sync")
    public R<Map<String, Integer>> sync() {
        return R.ok(githubRepoService.sync());
    }
}
