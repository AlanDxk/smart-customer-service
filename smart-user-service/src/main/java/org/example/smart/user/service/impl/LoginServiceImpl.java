package org.example.smart.user.service.impl;

import org.example.smart.common.response.ApiResponse;
import org.example.smart.gateway.util.JwtUtil;
import org.example.smart.user.dto.LoginRequest;
import org.example.smart.user.dto.LoginResponse;
import org.example.smart.user.entity.User;
import org.example.smart.user.repository.UserRepository;
import org.example.smart.user.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    @Autowired
    public LoginServiceImpl(JwtUtil jwtUtil, UserRepository userRepository, ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<ApiResponse<LoginResponse>> login(LoginRequest loginRequest) {

        // 1. 参数验证
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (username == null || username.trim().isEmpty()) {
            return Mono.just(ApiResponse.error("用户名不能为空"));
        }
        if (password == null || password.trim().isEmpty()) {
            return Mono.just(ApiResponse.error("密码不能为空"));
        }

        // 2. 查询用户
        Mono<User> findUser = userRepository.findByUsername(username);

        // 3. 验证用户
        Mono<User> validateUser = findUser.flatMap(user -> doValidateUser(user, password));

        // 4. 更新登录信息
        Mono<User> updateUser = validateUser.flatMap(this::doUpdateLoginInfo);

        // 5. 生成Token
        Mono<LoginResponse> generateToken = updateUser.flatMap(this::doGenerateTokens);

        // 6. 包装成功响应
        Mono<ApiResponse<LoginResponse>> successResponse = generateToken.map(ApiResponse::success);

        // 7. 返回结果（成功或失败）
        return successResponse.switchIfEmpty(Mono.just(ApiResponse.error("用户名或密码错误")));
    }

    private Mono<User> doValidateUser(User user, String password) {
        if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
            logger.warn("用户已被删除: {}", user.getUsername());
            return Mono.empty();
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            logger.warn("用户状态异常: {}, status={}", user.getUsername(), user.getStatus());
            return Mono.empty();
        }
        if (!user.getPassword().equals(password)) {
            logger.warn("密码错误: {}", user.getUsername());
            return Mono.empty();
        }
        logger.info("用户验证成功: {}", user.getUsername());
        return Mono.just(user);
    }

    private Mono<User> doUpdateLoginInfo(User user) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount((user.getLoginCount() == null ? 0 : user.getLoginCount()) + 1);
        user.setUpdateTime(LocalDateTime.now());

        return userRepository.save(user)
                .doOnSuccess(saved -> logger.info("更新登录信息成功: {}", saved.getUsername()))
                .onErrorResume(e -> {
                    logger.error("更新登录信息失败: {}", user.getUsername(), e);
                    return Mono.just(user);
                });
    }

    private Mono<LoginResponse> doGenerateTokens(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userId", user.getId().toString());
        claims.put("role", getUserRole(user.getStatus()));
        claims.put("email", user.getEmail());

        String accessToken = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId().toString())
                .username(user.getUsername())
                .role(getUserRole(user.getStatus()))
                .build();

        String userId = user.getId().toString();
        String redisKey = "refresh:token:" + userId;
        Duration expiration = Duration.ofMillis(refreshTokenExpiration);

        return reactiveRedisTemplate.opsForValue()
                .set(redisKey, refreshToken, expiration)
                .doOnSuccess(success -> logger.info("RefreshToken存储成功: userId={}, key={}", userId, redisKey))
                .doOnError(error -> logger.error("RefreshToken存储失败: userId={}, key={}", userId, redisKey, error))
                .thenReturn(response);
    }

    private String getUserRole(Integer status) {
        if (status == null) return "普通用户";
        return switch (status) {
            case 0 -> "普通用户";
            case 1 -> "管理员";
            case 2 -> "超级管理员";
            default -> "普通用户";
        };
    }
}