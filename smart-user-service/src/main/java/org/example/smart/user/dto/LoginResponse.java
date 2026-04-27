package org.example.smart.user.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String userId;
    private String username;
    private String role;
}