package com.topwheat.pestdetect.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * JWT工具类
 * Feature: User Authentication
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret:wheatPestSecretKey2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private long expiration;

    // Token黑名单（生产环境应使用Redis）
    private final Set<String> tokenBlacklist = new HashSet<>();

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT Token
     */
    public String generateToken(String userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 从Token中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        // 检查是否在黑名单中
        if (tokenBlacklist.contains(token)) {
            return false;
        }

        try {
            Claims claims = parseToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 使Token失效（登出时使用）
     */
    public void invalidateToken(String token) {
        tokenBlacklist.add(token);
    }

    /**
     * 解析Token
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("Token已过期: " + e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            System.out.println("不支持的Token: " + e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            System.out.println("Token格式错误: " + e.getMessage());
            return null;
        } catch (SignatureException e) {
            System.out.println("Token签名验证失败: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println("Token为空或非法: " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查Token是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 获取Token剩余有效时间（毫秒）
     */
    public long getExpirationTime(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return 0;
        }
        Date expiration = claims.getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
