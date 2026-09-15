package com.hotel.pms.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * <p>
 * 负责JWT令牌的生成、解析和验证
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtUtil {
    
    @Value("${pms.auth.token-secret:pms-hotel-secret-key-at-least-32-chars}")
    private String secret;
    
    @Value("${pms.auth.token-expire:86400}")
    private long expireSeconds;
    
    /**
     * 生成JWT令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @param hotelId 酒店ID
     * @return JWT令牌
     */
    public String generateToken(Long userId, String username, String role, Long hotelId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("hotelId", hotelId);
        
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireSeconds * 1000))
                .signWith(getSecretKey())
                .compact();
    }
    
    /**
     * 解析JWT令牌
     * 
     * @param token JWT令牌
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 验证JWT令牌
     * 
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期：{}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("JWT令牌无效：{}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
    
    /**
     * 从令牌中获取角色
     * 
     * @param token JWT令牌
     * @return 角色
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * 从令牌中获取酒店ID
     * 
     * @param token JWT令牌
     * @return 酒店ID
     */
    public Long getHotelId(String token) {
        Claims claims = parseToken(token);
        return claims.get("hotelId", Long.class);
    }
    
    /**
     * 获取密钥
     * 
     * @return SecretKey
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}