package com.topwheat.pestdetect.controller;

import com.topwheat.pestdetect.model.User;
import com.topwheat.pestdetect.service.UserService;
import com.topwheat.pestdetect.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * Feature: User Authentication
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // 参数校验
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("用户名不能为空"));
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("密码不能为空"));
        }

        // 调用服务层验证
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        
        if (user == null) {
            return ResponseEntity.status(401).body(createErrorResponse("用户名或密码错误"));
        }

        // 检查账号是否被锁定
        if (user.isLocked()) {
            return ResponseEntity.status(403).body(createErrorResponse("账号已锁定，请30分钟后重试"));
        }

        // 生成JWT Token
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getUserRole());

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("userInfo", createUserInfo(user));

        return ResponseEntity.ok(response);
    }

    /**
     * 用户登出
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        // 将Token加入黑名单
        jwtUtils.invalidateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(createSuccessResponse("登出成功"));
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/current-user
     */
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        if (!jwtUtils.validateToken(jwt)) {
            return ResponseEntity.status(401).body(createErrorResponse("Token无效或已过期"));
        }

        String username = jwtUtils.getUsernameFromToken(jwt);
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return ResponseEntity.status(404).body(createErrorResponse("用户不存在"));
        }

        return ResponseEntity.ok(createUserInfo(user));
    }

    // ============ 私有方法 ============

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getUserRole());
        userInfo.put("email", user.getEmail());
        return userInfo;
    }

    /**
     * 登录请求DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;
        private Boolean rememberMe;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Boolean getRememberMe() { return rememberMe; }
        public void setRememberMe(Boolean rememberMe) { this.rememberMe = rememberMe; }
    }
}
