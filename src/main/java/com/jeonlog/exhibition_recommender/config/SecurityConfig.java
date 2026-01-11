package com.jeonlog.exhibition_recommender.config;

import com.jeonlog.exhibition_recommender.auth.config.JwtAuthenticationFilter;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.handler.OAuth2JwtSuccessHandler;
import com.jeonlog.exhibition_recommender.auth.service.CustomOAuth2UserService;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ 기본 설정
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login/**", "/css/**", "/js/**", "/images/**",
                                "/favicon.ico",
                                "/error",
                                "/oauth2/**", "/oauth2/redirect/**",
                                "/api/oauth/**",          // ✅ TempToken 온보딩용 공개 경로
                                "/api/auth/**",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/swagger-ui.html", "/webjars/**",
                                "/api/health"                   // ✅ ELB 헬스체크용
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )

                // ✅ 인증 실패 시 401 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )

                // ✅ OAuth2 로그인
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(info -> info.userService(customOAuth2UserService))
                        .successHandler(oAuth2JwtSuccessHandler)
                )

                // ✅ 로그아웃 설정
                .logout(logout -> logout.logoutSuccessUrl("/"));

        // ✅ JWT 필터 등록
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:8081",             // RN 로컬 개발 환경
                "https://jeonlog.com",              // 운영 도메인
                "https://api.jeonlog.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Location", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}