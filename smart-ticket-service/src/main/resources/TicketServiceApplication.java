package org.example.smart.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 智能客服系统 - 工单服务
 * 核心功能：
 * 1. 工单创建和管理
 * 2. 智能工单路由
 * 3. 工单状态跟踪
 * 4. 工单统计分析
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class TicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
        System.out.println("工单服务启动成功！");
    }
}