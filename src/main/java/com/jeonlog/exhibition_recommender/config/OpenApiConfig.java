package com.jeonlog.exhibition_recommender.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("전:록 API")
                        .description("전시 추천 플랫폼 백엔드 API 명세")
                        .version("v1.0.0"))
                // ✅ JWT 인증 스키마 추가
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Access Token 입력 (예: 'Bearer {token}')")))
                // ✅ 모든 요청에 JWT 인증 적용
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}