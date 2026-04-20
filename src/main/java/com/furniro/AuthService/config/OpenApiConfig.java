package com.furniro.AuthService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service")
                        .version("1.0")
                        .description("Auth Service"))
                .addServersItem(new Server()
                        .url("/api/v1/furniro/auth-service")
                        .description("Gateway Server"));
    }
}