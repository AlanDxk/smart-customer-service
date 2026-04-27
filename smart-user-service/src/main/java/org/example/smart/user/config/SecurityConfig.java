package org.example.smart.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                // 这些路径完全放行，不需要认证
                .pathMatchers(
                        "/api/users/login",          // ← 关键：放行你的登录接口
                        "/api/users/register",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**"
                ).permitAll()
                // 你自定义的 API 路径（按需放行或要求认证）
                .pathMatchers("/api/public/**").permitAll()
                // 其他所有请求都需要认证
                .anyExchange().authenticated()
            )
            .httpBasic(httpBasic -> {})    // 启用 HTTP Basic 认证
            .formLogin(formLogin -> {})    // 如果你需要表单登录页的话
            .csrf(csrf -> csrf.disable())  // 开发阶段建议关闭
            .build();
    }
}