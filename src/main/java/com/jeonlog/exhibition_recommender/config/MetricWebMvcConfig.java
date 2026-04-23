package com.jeonlog.exhibition_recommender.config;

import com.jeonlog.exhibition_recommender.common.metric.MetricRecorder;
import com.jeonlog.exhibition_recommender.common.metric.UserActivityInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class MetricWebMvcConfig implements WebMvcConfigurer {

    private final MetricRecorder metricRecorder;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserActivityInterceptor(metricRecorder))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/oauth/**",
                        "/api/metrics/**"
                );
    }
}
