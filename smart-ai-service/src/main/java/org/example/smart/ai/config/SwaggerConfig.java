package org.example.smart.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:smart-ai-service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智能客服AI服务API")
                        .version("1.0")
                        .description("智能客服AI服务接口文档")
                        .contact(new Contact()
                                .name("AI开发团队")
                                .email("ai-dev@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地开发环境（通过网关访问）"),
                        new Server()
                                .url("http://dev.example.com")
                                .description("开发环境（通过网关访问）"),
                        new Server()
                                .url("http://prod.example.com")
                                .description("生产环境（通过网关访问）")
                ));
    }
}