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
        //4、获取用户信息
        String userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
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
        if(jwtUtil.isTokenAboutToExpire(token)){
            String newToken=renewAccessToken(userId,claims).block();
            if (newToken == null) {
                return unauthorized(exchange, "续约AccessToken失败");
            }
            mutatedExchange.getAttributes().put("newToken", newToken);
            logger.debug("Token即将过期，已生成新Token - userId: {}", userId);

        }
        // 7. 继续过滤链，并在响应时添加新Token
        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            String newToken = mutatedExchange.getAttribute("newToken");
            if (newToken != null) {
                ServerHttpResponse response = exchange.getResponse();
                response.getHeaders().add("X-New-Token", newToken);
                logger.debug("响应头已添加新Token - userId: {}", userId);
            }
        }));
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

    private Mono<String> renewAccessToken(String userId,Claims claims) {
        //1、先从redis中获取Refresh token
        return reactiveRedisTemplate.opsForValue().get("refresh:token:" + userId)
            .switchIfEmpty(Mono.defer(()->{
                logger.error("未找到Refresh token");
                return Mono.empty();
            }))
            .flatMap(longToken->{
                //2、判断是否过期，过期要重新登录
                if(jwtUtil.isTokenExpired(longToken)){
                    logger.error("Refresh token过期");
                    return Mono.empty();
                }
                //3、没有过期，生成新的AccessToken返回
                String newToken = jwtUtil.generateAccessToken(claims);
                return Mono.just(newToken);
            });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}