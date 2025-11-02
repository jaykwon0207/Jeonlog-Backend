package com.jeonlog.exhibition_recommender.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        final String scheme = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("전:록 API")
                        .description("전시 추천 플랫폼 백엔드 API 명세")
                        .version("v1.0.0"));
    }
}
