package com.jeonlog.exhibition_recommender.auth.config;

import com.jeonlog.exhibition_recommender.auth.model.CustomUserDetails;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // ✅ 로그인, OAuth2, 에러, 헬스체크 등은 필터 통과
        if (uri.startsWith("/login") || uri.startsWith("/oauth2") ||
                uri.startsWith("/error") || uri.startsWith("/api/health") ||
                uri.equals("/favicon.ico")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                if (email != null) {
                    userRepository.findByEmail(email).ifPresent(user -> {
                        CustomUserDetails cud = new CustomUserDetails(user);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(cud, null, cud.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
            }
        } catch (Exception e) {
            log.error("❌ JWT Authentication Filter Error: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }
}