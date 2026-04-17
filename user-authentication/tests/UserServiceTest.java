package com.topwheat.pestdetect.service;

import com.topwheat.pestdetect.model.User;
import com.topwheat.pestdetect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务层单元测试
 * Feature: User Authentication
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId("U001");
        mockUser.setUsername("admin");
        mockUser.setPassword(passwordEncoder.encode("admin123"));
        mockUser.setUserRole("ADMIN");
        mockUser.setLoginFailCount(0);
    }

    @Test
    void testAuthenticateSuccess() {
        // Given
        when(userRepository.findByUsername("admin")).thenReturn(mockUser);

        // When
        User result = userService.authenticate("admin", "admin123");

        // Then
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAuthenticateWrongPassword() {
        // Given
        when(userRepository.findByUsername("admin")).thenReturn(mockUser);

        // When
        User result = userService.authenticate("admin", "wrongpassword");

        // Then
        assertNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAuthenticateUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        // When
        User result = userService.authenticate("nonexistent", "password");

        // Then
        assertNull(result);
    }

    @Test
    void testAccountLockAfterMaxFailures() {
        // Given
        mockUser.setLoginFailCount(4);
        when(userRepository.findByUsername("admin")).thenReturn(mockUser);

        // When
        userService.authenticate("admin", "wrongpassword");

        // Then
        assertTrue(mockUser.isLocked());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testValidatePasswordStrength() {
        // Valid passwords
        assertNull(userService.validatePasswordStrength("Password123!"));
        assertNull(userService.validatePasswordStrength("MyP@ssw0rd"));

        // Invalid passwords
        assertEquals("密码长度至少8位", userService.validatePasswordStrength("Pass1!"));
        assertEquals("密码必须包含大写字母", userService.validatePasswordStrength("password123!"));
        assertEquals("密码必须包含小写字母", userService.validatePasswordStrength("PASSWORD123!"));
        assertEquals("密码必须包含数字", userService.validatePasswordStrength("Password!@#"));
        assertEquals("密码必须包含特殊字符", userService.validatePasswordStrength("Password123"));
    }

    @Test
    void testCreateUser() {
        // Given
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.createUser("newuser", "Password123!", "VIEWER");

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("VIEWER", result.getUserRole());
        assertNotNull(result.getUserId());
        assertTrue(result.getStatus() == 1);
    }

    @Test
    void testChangePasswordSuccess() {
        // Given
        when(userRepository.findById("U001")).thenReturn(java.util.Optional.of(mockUser));

        // When
        boolean result = userService.changePassword("U001", "admin123", "NewPass123!");

        // Then
        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangePasswordWrongOldPassword() {
        // Given
        when(userRepository.findById("U001")).thenReturn(java.util.Optional.of(mockUser));

        // When
        boolean result = userService.changePassword("U001", "wrongpassword", "NewPass123!");

        // Then
        assertFalse(result);
    }

    @Test
    void testUnlockUser() {
        // Given
        mockUser.setLoginFailCount(5);
        mockUser.setLockTime(new java.util.Date());
        when(userRepository.findById("U001")).thenReturn(java.util.Optional.of(mockUser));

        // When
        userService.unlockUser("U001");

        // Then
        assertEquals(0, mockUser.getLoginFailCount());
        assertNull(mockUser.getLockTime());
        verify(userRepository).save(any(User.class));
    }
}
