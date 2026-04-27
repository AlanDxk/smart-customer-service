package org.example.smart.gateway.util;

import io.jsonwebtoken.Claims;
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


/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Value("${jwt.access-token-expiration}")
    private long expirationTime;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationTime;

    /**
     * 生成访问令牌
     * @param claims 自定义claims
     * @return JWT token
     */
    public String generateAccessToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成刷新令牌
     * @param claims 自定义claims
     * @return 刷新token
     */
    public String generateRefreshToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .signWith(getSigningKey())
                .compact();
    }



    /**
     * 验证JWT token
     * @param token JWT token
     * @return 验证结果，成功返回claims，失败返回null
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("JWT验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查token是否过期
     * @param token JWT token
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return true;
        }
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }



    /**
     * 从token中获取用户ID
     * @param token JWT token
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("userId", String.class);
    }

    /**
     * 从token中获取用户名
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("username", String.class);
    }

    /**
     * 从token中获取用户角色
     * @param token JWT token
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("role", String.class);
    }

    /**
     * 检查token是否即将过期（剩余时间小于30分钟）
     * @param token JWT token
     * @return true-即将过期，false-未过期
     */
    public boolean isTokenAboutToExpire(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return true;
        }
        Date expiration = claims.getExpiration();
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        return remainingTime < 30 * 60 * 1000; // 30分钟
    }

    /**
     * 获取token的过期时间
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return claims.getExpiration();
    }

    /**
     * 获取token的签发时间
     * @param token JWT token
     * @return 签发时间
     */
    public Date getIssuedAtDateFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return claims.getIssuedAt();
    }
}