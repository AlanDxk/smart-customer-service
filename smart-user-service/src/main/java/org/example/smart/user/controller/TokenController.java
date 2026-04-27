package org.example.smart.user.controller;

import org.example.smart.gateway.util.JwtUtil;
import org.example.smart.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Token控制器
 */
@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 验证token
     * @param token JWT token
     * @return 验证结果
     */
    @GetMapping("/validate")
    public ApiResponse<Object> validateToken(@RequestParam String token) {
        if (jwtUtil.validateToken(token) != null) {
            return ApiResponse.success("Token有效");
        } else {
            return ApiResponse.error("Token无效或已过期");
        }
    }

    /**
     * 检查token是否过期
     * @param token JWT token
     * @return 过期状态
     */
    @GetMapping("/check-expired")
    public ApiResponse<Object> checkTokenExpired(@RequestParam String token) {
        boolean isExpired = jwtUtil.isTokenExpired(token);
        return ApiResponse.success(isExpired ? "Token已过期" : "Token未过期");
    }

    /**
     * 获取token中的用户信息
     * @param token JWT token
     * @return 用户信息
     */
    @GetMapping("/info")
    public ApiResponse<Object> getTokenInfo(@RequestParam String token) {
        String userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        if (userId == null || username == null) {
            return ApiResponse.error("无法获取用户信息");
        }

        return ApiResponse.success(Map.of(
                "userId", userId,
                "username", username,
                "role", role
        ));
    }
}