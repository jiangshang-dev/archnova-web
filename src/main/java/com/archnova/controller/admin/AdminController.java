package com.archnova.controller.admin;

import com.archnova.common.PageResult;
import com.archnova.common.R;
import com.archnova.domain.base.LoginUser;
import com.archnova.domain.entity.ContactMessage;
import com.archnova.domain.entity.ProjectCase;
import com.archnova.mapper.ProjectCaseMapper;
import com.archnova.service.AuthService;
import com.archnova.service.ChatService;
import com.archnova.service.ContactService;
import com.archnova.service.ProjectCaseService;
import com.archnova.service.SiteConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;
    private final SiteConfigService siteConfigService;
    private final ProjectCaseService projectCaseService;
    private final ContactService contactService;
    private final ChatService chatService;
    private final ProjectCaseMapper projectCaseMapper;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return R.ok(authService.login(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/me")
    public R<LoginUser> me() {
        return R.ok(authService.current());
    }

    @PutMapping("/password")
    public R<Void> password(@RequestBody PasswordRequest request) {
        authService.changePassword(request.getOldPassword(), request.getNewPassword());
        return R.ok();
    }

    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("caseCount", projectCaseMapper.selectCount(null));
        data.put("unreadContact", contactService.unreadCount());
        data.put("openChat", chatService.openCount());
        data.put("recentContacts", contactService.page(1, 5, null).getRecords());
        return R.ok(data);
    }

    @GetMapping("/config")
    public R<Map<String, String>> config() {
        return R.ok(siteConfigService.publicConfig());
    }

    @PutMapping("/config")
    public R<Void> updateConfig(@RequestBody Map<String, String> values) {
        siteConfigService.update(values);
        return R.ok();
    }

    @GetMapping("/cases")
    public R<PageResult<ProjectCase>> cases(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Integer status) {
        return R.ok(projectCaseService.page(page, size, keyword, status));
    }

    @PostMapping("/cases")
    public R<Void> saveCase(@RequestBody ProjectCase projectCase) {
        projectCaseService.save(projectCase);
        return R.ok();
    }

    @PostMapping("/cases/{id}/delete")
    public R<Void> deleteCase(@PathVariable Long id) {
        projectCaseService.delete(id);
        return R.ok();
    }

    @GetMapping("/contacts")
    public R<PageResult<ContactMessage>> contacts(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) Integer status) {
        return R.ok(contactService.page(page, size, status));
    }

    @PostMapping("/contacts/{id}/read")
    public R<Void> readContact(@PathVariable Long id) {
        contactService.markRead(id);
        return R.ok();
    }

    @GetMapping("/chat/sessions")
    public R<Object> sessions() {
        return R.ok(chatService.sessions());
    }

    @GetMapping("/chat/sessions/{id}/messages")
    public R<Object> messages(@PathVariable Long id) {
        return R.ok(chatService.history(id, true));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class PasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}
