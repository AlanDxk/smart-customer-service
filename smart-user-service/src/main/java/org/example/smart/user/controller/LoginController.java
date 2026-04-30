package org.example.smart.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.smart.user.dto.LoginRequest;
import org.example.smart.user.dto.LoginResponse;
import org.example.smart.user.service.LoginService;
import org.example.smart.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 用户控制器（响应式）
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户登录、注册、信息管理等接口")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService userService;

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录验证，成功后返回JWT令牌")
    public Mono<ApiResponse<LoginResponse>> login(
            @Parameter(description = "登录请求参数", required = true)
            @RequestBody Mono<LoginRequest> loginRequest) {
        return loginRequest.flatMap(userService::login);
    }
}