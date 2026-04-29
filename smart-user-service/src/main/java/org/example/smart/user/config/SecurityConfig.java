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
                // 所有路径都放行，不需要认证
                .anyExchange().permitAll()
            )
            .httpBasic(httpBasic -> httpBasic.disable())    // 禁用 HTTP Basic 认证
            .formLogin(formLogin -> formLogin.disable())    // 禁用表单登录
            .csrf(csrf -> csrf.disable())                  // 禁用 CSRF
            .build();
    }
}