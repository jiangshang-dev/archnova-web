package com.archnova.controller.open;

import com.archnova.common.R;
import com.archnova.domain.entity.ContactMessage;
import com.archnova.domain.entity.ProjectCase;
import com.archnova.service.ContactService;
import com.archnova.service.GithubService;
import com.archnova.service.ProjectCaseService;
import com.archnova.service.SiteConfigService;
import com.archnova.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/open")
@RequiredArgsConstructor
public class OpenSiteController {

    private final SiteConfigService siteConfigService;
    private final ProjectCaseService projectCaseService;
    private final GithubService githubService;
    private final ContactService contactService;

    @GetMapping("/site")
    public R<Map<String, String>> site() {
        return R.ok(siteConfigService.publicConfig());
    }

    @GetMapping("/cases")
    public R<List<ProjectCase>> cases(@RequestParam(required = false) String category) {
        return R.ok(projectCaseService.published(category));
    }

    @GetMapping("/cases/{id}")
    public R<ProjectCase> caseDetail(@PathVariable Long id) {
        return R.ok(projectCaseService.publishedDetail(id));
    }

    @GetMapping("/github")
    public R<List<Map<String, Object>>> github() {
        return R.ok(githubService.repos());
    }

    @PostMapping("/contact")
    public R<Void> contact(@RequestBody ContactMessage message, HttpServletRequest request) {
        contactService.submit(message, IpUtils.getIpAddr(request));
        return R.ok();
    }
}
