package org.example.smart.gateway.filter;


import io.jsonwebtoken.Claims;
import org.example.smart.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthGlobalFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Value("${jwt.renewal-threshold-minutes:30}")
    private long renewalThresholdMinutes;  // 续约阈值（分钟）

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/api/users/login"

    );

    // 路径匹配器
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 0、获取请求路径和方法
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        logger.info("========== JwtAuthGlobalFilter 拦截到请求: {} {} ==========", method, path);

        // 1、放行OPTIONS预检请求（CORS）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("放行OPTIONS预检请求: {}", path);
            return chain.filter(exchange);
        }

        // 2、检查是否需要放行
        if (isExcludedPath(path)) {
            logger.debug("放行路径: {}", path);
            return chain.filter(exchange);
        }
        //2、提取token
        String token = extractToken(request);
        if (token == null) {
            logger.error("未找到token");
            return unauthorized(exchange, "缺少认证Token");
        }
        //3、验证token
        Claims claims = jwtUtil.validateToken(token);
        if (claims == null) {
            logger.error("token验证失败");
            return unauthorized(exchange, "token验证失败");
        }
        //4、从claims直接获取用户信息（不再重复解析token）
        String userId = jwtUtil.getUserId(claims);
        String username = jwtUtil.getUsername(claims);
        String role = jwtUtil.getRole(claims);
        if (userId == null || username == null || role == null) {
            logger.error("从token中获取用户信息失败");
            return unauthorized(exchange, "从token中获取用户信息失败");
        }
        logger.debug("Token验证通过 - userId: {}, username: {}, role: {}", userId, username, role);
        // 5. 构建新的请求（添加用户信息Header）
        ServerHttpRequest newRequest = request.mutate()
                .header("userId", userId)
                .header("username", username)
                .header("role", role)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(newRequest)
                .build();
        // 6. Token续约逻辑
        boolean aboutToExpire = jwtUtil.isTokenAboutToExpire(claims);
        boolean expired = jwtUtil.isTokenExpired(claims);
        logger.info("[续约-检查] aboutToExpire={}, expired={}, userId={}", aboutToExpire, expired, userId);

        if(aboutToExpire){

            return renewAccessToken(userId, claims)
                // 用 Optional 包装，使 defaultIfEmpty 只对 renewAccessToken 生效
                // 避免 chain.filter 返回 Mono.empty() 时误触发 fallback
                .map(newAccessToken -> java.util.Optional.of(newAccessToken))
                .defaultIfEmpty(java.util.Optional.empty())
                .flatMap(optToken -> {
                    if (optToken.isPresent()) {
                        String newToken = optToken.get();
                        logger.info("Token续约成功，更新请求头 - userId: {}", userId);

                        // 将新 token 更新到请求头中，替换旧 token
                        ServerHttpRequest renewedRequest = newRequest.mutate()
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken)
                                .build();

                        // 用装饰器包装响应，使 ReadOnlyHttpHeaders 变为可写
                        ServerHttpResponse decoratedResponse = wrapResponse(
                                mutatedExchange.getResponse(), "X-New-Token", newToken);

                        ServerWebExchange newExchange = mutatedExchange.mutate()
                                .request(renewedRequest)
                                .response(decoratedResponse)
                                .build();

                        return chain.filter(newExchange);
                    } else {
                        // renewAccessToken 返回空，续约失败
                        if (expired) {
                            logger.error("Token已过期且续约失败 - userId: {}", userId);
                            return unauthorized(exchange, "Token已过期，请重新登录");
                        }
                        logger.warn("Token续约失败，继续处理请求 - userId: {}", userId);
                        return chain.filter(mutatedExchange);
                    }
                });
        }
        // 7. 继续过滤链
        return chain.filter(mutatedExchange);
    }




    /**
     * 检查路径是否需要放行
     * @param path
     * @return
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求头中提取token
     * @param request
     * @return
     */
    private String extractToken(ServerHttpRequest request) {
        // 方式1：Authorization Header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 方式2：自定义Header
        String token = request.getHeaders().getFirst("X-Auth-Token");
        if (token != null) {
            return token;
        }
        return null;
    }

    /**
     * 处理未授权异常
     * @param exchange
     * @param message
     * @return
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = String.format("{\"code\": 401, \"message\": \"%s\", \"timestamp\": %d}",
            message, System.currentTimeMillis());

        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory()
            .wrap(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 包装响应，使其响应头可写，用于在续约时添加 X-New-Token
     */
    private ServerHttpResponse wrapResponse(ServerHttpResponse delegate, String header, String value) {
        return new ServerHttpResponseDecorator(delegate) {
            private HttpHeaders writableHeaders;

            @Override
            public HttpHeaders getHeaders() {
                if (this.writableHeaders == null) {
                    // 从只读headers复制一份到可写的 HttpHeaders
                    this.writableHeaders = new HttpHeaders();
                    this.writableHeaders.addAll(super.getHeaders());
                    this.writableHeaders.add(header, value);
                }
                return this.writableHeaders;
            }
        };
    }

    private Mono<String> renewAccessToken(String userId, Claims claims) {
        String redisKey = "refresh:token:" + userId;

        return reactiveRedisTemplate.opsForValue().get(redisKey)
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("[续约-步骤1] Redis中未找到refresh token, userId={}", userId);
                    return Mono.empty();
                }))
                .flatMap(refreshToken -> {
                    logger.info("[续约-步骤1] 从Redis获取到refresh token, userId={}", userId);
                    if (jwtUtil.isTokenExpired(refreshToken)) {
                        logger.warn("[续约-步骤2] Refresh token已过期, userId={}", userId);
                        return Mono.empty();
                    }
                    logger.info("[续约-步骤2] Refresh token有效, userId={}", userId);
                    String newToken = jwtUtil.generateAccessToken(claims);
                    logger.info("[续约-步骤3] 新access token已生成, userId={}", userId);
                    return Mono.just(newToken);
                })
                .onErrorResume(e -> {
                    logger.error("[续约-异常] userId={}, error={}", userId, e.getMessage(), e);
                    return Mono.empty();
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}