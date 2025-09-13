package com.jeonlog.exhibition_recommender.config;

import com.jeonlog.exhibition_recommender.auth.config.JwtAuthenticationFilter;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.handler.OAuth2JwtSuccessHandler;
import com.jeonlog.exhibition_recommender.auth.service.CustomOAuth2UserService;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2JwtSuccessHandler oAuth2JwtSuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ CORS 켜기
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login/**", "/css/**", "/js/**", "/images/**",
                                "/oauth/add-info", "/error", "/oauth2/**", "/oauth2/redirect/**"
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll() // ✅ 프리플라이트 허용
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(info -> info.userService(customOAuth2UserService))
                        .successHandler(oAuth2JwtSuccessHandler)
                )
                .logout(logout -> logout.logoutSuccessUrl("/"));

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }


    /**
     * ✅ CORS 정책
     * - 개발: React 로컬(http://localhost:3000) 허용
     * - 운영: 배포된 프런트엔드 도메인 추가(예: https://my-frontend.com)
     * - JWT 헤더 노출(Authorization) + Credentials 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 프론트엔드 출처 목록 (개발/운영 도메인 필요시 추가)
        config.setAllowedOriginPatterns(List.of(
                        "http://localhost:8081"
                        // , "https://my-frontend.com"
                )
        );

        // 허용 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용/노출 헤더
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Location", "Content-Disposition"));

        // 인증정보(쿠키/Authorization) 허용
        config.setAllowCredentials(true);

        // (선택) 프리플라이트 캐시 시간
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}