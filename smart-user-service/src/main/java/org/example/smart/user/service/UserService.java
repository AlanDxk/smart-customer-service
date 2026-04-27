package org.example.smart.user.service;

import org.example.smart.user.dto.LoginRequest;
import org.example.smart.user.dto.LoginResponse;
import org.example.smart.user.entity.User;
import org.example.smart.user.repository.UserRepository;
import org.example.smart.gateway.util.JwtUtil;
import org.example.smart.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务类
 */

public interface UserService {


    ApiResponse<LoginResponse> login(LoginRequest loginRequest);
}