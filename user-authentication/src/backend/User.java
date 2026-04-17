package com.topwheat.pestdetect.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 * 用户实体类
 * Feature: User Authentication
 */
@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(name = "user_id", length = 32)
    private String userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "user_role", length = 20)
    private String userRole;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "login_fail_count")
    private Integer loginFailCount = 0;

    @Column(name = "lock_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockTime;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    /**
     * 检查账号是否被锁定
     */
    public boolean isLocked() {
        if (lockTime == null) {
            return false;
        }
        // 锁定30分钟后自动解锁
        long lockDuration = 30 * 60 * 1000;
        return System.currentTimeMillis() - lockTime.getTime() < lockDuration;
    }
}
