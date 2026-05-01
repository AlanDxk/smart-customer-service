package org.example.smart.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long expirationTime;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationTime;

    @Value("${jwt.renewal-threshold-minutes:30}")
    private long renewalThresholdMinutes;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== Token 生成 ====================

    public String generateAccessToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAccessToken(Claims claims) {
        return generateAccessToken((Map<String, Object>) claims);
    }

    public String generateRefreshToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    // ==================== Token 验证 ====================

    /**
     * 验证JWT token并返回claims。
     * 过期但签名合法的token仍会返回claims，由调用方决定是否续约。
     * 签名不合法返回null。
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT已过期，尝试续约");
            return e.getClaims();
        } catch (Exception e) {
            logger.error("JWT验证失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== Claims 提取（直接传Claims，避免重复解析） ====================

    public String getUserId(Claims claims) {
        return claims.get("userId", String.class);
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    // ==================== Token 状态判断（Claims 版本，推荐使用） ====================

    /**
     * 检查token是否已过期（当前时间已超过过期时间）
     */
    public boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 检查token是否即将过期（剩余时间小于续约阈值）
     */
    public boolean isTokenAboutToExpire(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return false;
        }
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        return remainingTime < renewalThresholdMinutes * 60 * 1000;
    }

    // ==================== Token 状态判断（String 版本，内部调用时请优先用Claims版本） ====================

    public boolean isTokenExpired(String token) {
        Claims claims = validateToken(token);
        return claims == null || isTokenExpired(claims);
    }

    public boolean isTokenAboutToExpire(String token) {
        Claims claims = validateToken(token);
        return claims != null && isTokenAboutToExpire(claims);
    }

    // ==================== 从 token 字符串提取信息（String 版本） ====================

    public String getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? getUserId(claims) : null;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? getUsername(claims) : null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? getRole(claims) : null;
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    public Date getIssuedAtDateFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? claims.getIssuedAt() : null;
    }
}