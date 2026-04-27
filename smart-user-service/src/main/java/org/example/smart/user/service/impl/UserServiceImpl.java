package org.example.smart.user.service.impl;

import org.example.smart.common.response.ApiResponse;
import org.example.smart.gateway.util.JwtUtil;
import org.example.smart.user.dto.LoginRequest;
import org.example.smart.user.dto.LoginResponse;
import org.example.smart.user.entity.User;
import org.example.smart.user.repository.UserRepository;
import org.example.smart.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    /**
     * 用户登录验证
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest loginRequest) {
        // 参数验证
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return ApiResponse.error("密码不能为空");
        }

        // 验证用户名和密码
        User user = validateUser(loginRequest.getUsername(), loginRequest.getPassword());
        if (user == null) {
            return ApiResponse.error("用户名或密码错误");
        }

        // 更新登录信息
        updateLoginInfo(user);

        // 生成JWT token
        LoginResponse response = generateTokens(user);
        logger.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());
        return ApiResponse.success(response);
    }

    /**
     * 验证用户名和密码
     * @param username 用户名
     * @param password 密码
     * @return 验证通过返回用户对象，验证失败返回null
     */
    private User validateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("登录失败: 用户不存在, username={}", username);
            return null;
        }

        // 检查用户是否被删除
        if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
            logger.warn("登录失败: 用户已被删除, username={}", username);
            return null;
        }

        // 检查用户状态（0-正常，1-禁用，2-锁定）
        if (user.getStatus() == null || user.getStatus() != 0) {
            logger.warn("登录失败: 用户状态异常, username={}, status={}", username, user.getStatus());
            return null;
        }

        // 验证密码（明文对比）
        if (!user.getPassword().equals(password)) {
            logger.warn("登录失败: 密码错误, username={}", username);
            return null;
        }

        return user;
    }

    /**
     * 更新用户登录信息
     * @param user 用户对象
     */
    private void updateLoginInfo(User user) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount((user.getLoginCount() == null ? 0 : user.getLoginCount()) + 1);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 生成JWT token
     * @param user 用户对象
     * @return 登录响应
     */
    private LoginResponse generateTokens(User user) {
        LoginResponse response = new LoginResponse();

        // 创建claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userId", user.getId().toString());
        claims.put("role", getUserRole(user.getStatus()));
        claims.put("email", user.getEmail());

        // 生成访问令牌
        String accessToken = jwtUtil.generateAccessToken(claims);
        // 生成刷新令牌
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(7200L); // 2小时
        response.setUserId(user.getId().toString());
        response.setUsername(user.getUsername());
        response.setRole(getUserRole(user.getStatus()));

        return response;
    }

    /**
     * 获取用户角色
     * @param status 用户状态
     * @return 角色名称
     */
    private String getUserRole(Integer status) {
        if (status == null) {
            return "普通用户";
        }
        return switch (status) {
            case 0 -> "普通用户";
            case 1 -> "管理员";
            case 2 -> "超级管理员";
            default -> "普通用户";
        };
    }
}
