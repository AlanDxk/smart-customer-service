package org.example.smart.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.session.data.redis.config.annotation.EnableRedisHttpSession;

/**
 * 智能客服系统 - AI服务
 * 核心功能：
 * 1. 智能对话处理
 * 2. 工单智能分类
 * 3. 知识库检索
 * 4. 上下文管理
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableRedisHttpSession
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
        System.out.println("AI服务启动成功！");
    }
    
    @RestController
    static class HealthController {
        
        @GetMapping("/")
        public String home() {
            return "AI服务运行正常 - 智能客服系统";
        }
        
        @GetMapping("/health")
        public String health() {
            return "{\"status\": \"UP\", \"service\": \"smart-ai-service\"}";
        }
    }
}