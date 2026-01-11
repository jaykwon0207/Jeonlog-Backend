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

    // ⭐⭐⭐ 핵심 수정: temp-token API 필터 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return uri.startsWith("/api/oauth/add-info")
                || uri.startsWith("/api/oauth/check-nickname")
                || uri.startsWith("/api/oauth/apple")
                || uri.startsWith("/login")
                || uri.startsWith("/oauth2")
                || uri.startsWith("/error")
                || uri.startsWith("/api/health")
                || uri.equals("/")
                || uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (token.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);

                userRepository.findByEmail(email).ifPresent(user -> {
                    CustomUserDetails cud = new CustomUserDetails(user);

                    var auth = new UsernamePasswordAuthenticationToken(
                            cud, null, cud.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        } catch (Exception e) {
            log.error("❌ JWT Authentication Filter Error: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }
}