package com.topwheat.pestdetect.service;

import com.topwheat.pestdetect.model.User;
import com.topwheat.pestdetect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * 用户服务层
 * Feature: User Authentication
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 登录失败最大次数
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 用户认证
     * @param username 用户名
     * @param password 密码
     * @return 认证成功返回User，失败返回null
     */
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            return null;
        }

        // 检查账号是否被锁定
        if (user.isLocked()) {
            return user; // 返回用户但标记为锁定状态
        }

        // 验证密码
        if (passwordEncoder.matches(password, user.getPassword())) {
            // 登录成功，重置失败次数
            user.setLoginFailCount(0);
            user.setLockTime(null);
            userRepository.save(user);
            return user;
        } else {
            // 登录失败，增加失败次数
            incrementLoginFailCount(user);
            return null;
        }
    }

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 创建新用户
     */
    public User createUser(String username, String password, String role) {
        User user = new User();
        user.setUserId(UUID.randomUUID().toString().replace("-", ""));
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserRole(role);
        user.setStatus(1);
        user.setLoginFailCount(0);
        return userRepository.save(user);
    }

    /**
     * 修改密码
     */
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    /**
     * 增加登录失败次数
     */
    private void incrementLoginFailCount(User user) {
        int failCount = user.getLoginFailCount() + 1;
        user.setLoginFailCount(failCount);
        
        if (failCount >= MAX_LOGIN_FAIL_COUNT) {
            user.setLockTime(new Date());
        }
        
        userRepository.save(user);
    }

    /**
     * 解锁账号
     */
    public void unlockUser(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLoginFailCount(0);
            user.setLockTime(null);
            userRepository.save(user);
        }
    }

    /**
     * 验证密码强度
     * @return 验证通过返回null，否则返回错误信息
     */
    public String validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return "密码长度至少8位";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "密码必须包含大写字母";
        }
        if (!password.matches(".*[a-z].*")) {
            return "密码必须包含小写字母";
        }
        if (!password.matches(".*[0-9].*")) {
            return "密码必须包含数字";
        }
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            return "密码必须包含特殊字符";
        }
        return null;
    }
}
