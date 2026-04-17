package com.topwheat.pestdetect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.topwheat.pestdetect.model.User;
import com.topwheat.pestdetect.service.UserService;
import com.topwheat.pestdetect.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器单元测试
 * Feature: User Authentication
 */
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId("U001");
        mockUser.setUsername("admin");
        mockUser.setUserRole("ADMIN");
        mockUser.setEmail("admin@example.com");
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Given
        when(userService.authenticate("admin", "admin123")).thenReturn(mockUser);
        when(jwtUtils.generateToken(anyString(), anyString(), anyString()))
                .thenReturn("mock-jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.userInfo.username").value("admin"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        // Given
        when(userService.authenticate("admin", "wrongpassword")).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("用户名或密码错误"));
    }

    @Test
    void testLoginEmptyUsername() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"admin123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("用户名不能为空"));
    }

    @Test
    void testLoginEmptyPassword() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("密码不能为空"));
    }

    @Test
    void testLoginAccountLocked() throws Exception {
        // Given
        mockUser.setLoginFailCount(5);
        mockUser.setLockTime(new java.util.Date());
        when(userService.authenticate("admin", "admin123")).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("账号已锁定，请30分钟后重试"));
    }

    @Test
    void testLogoutSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }
}
