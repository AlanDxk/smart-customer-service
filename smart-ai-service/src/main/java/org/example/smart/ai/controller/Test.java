package org.example.smart.ai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public class Test {
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
