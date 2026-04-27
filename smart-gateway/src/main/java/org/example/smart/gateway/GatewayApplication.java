package org.example.smart.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 智能客服系统 - 网关服务
 * 核心功能：
 * 1. 路由转发
 * 2. 负载均衡
 * 3. 限流控制
 * 4. 熔断降级
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("网关服务启动成功！");
    }
}