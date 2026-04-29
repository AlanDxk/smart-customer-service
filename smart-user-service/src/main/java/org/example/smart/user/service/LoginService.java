package org.example.smart.user.service;

import org.example.smart.user.dto.LoginRequest;
import org.example.smart.user.dto.LoginResponse;
import org.example.smart.common.response.ApiResponse;
import reactor.core.publisher.Mono;

/**
 * 用户服务类（响应式）
 */
public interface LoginService {

    Mono<ApiResponse<LoginResponse>> login(LoginRequest loginRequest);
}