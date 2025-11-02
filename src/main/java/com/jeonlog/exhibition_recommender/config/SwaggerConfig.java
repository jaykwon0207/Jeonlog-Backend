package com.jeonlog.exhibition_recommender.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("전:록 Exhibition Recommendation API")
                        .description("전시 추천 플랫폼 전:록의 RESTful API 명세서입니다.")
                        .version("v1.0.0"));
    }
}
