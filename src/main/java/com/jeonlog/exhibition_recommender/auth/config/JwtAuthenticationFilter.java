package com.jeonlog.exhibition_recommender.auth.config;

import com.jeonlog.exhibition_recommender.auth.model.CustomUserDetails;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
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
public class
JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        log.info("[JWT FILTER CHECK] uri={}", uri);

        return uri.startsWith("/login/oauth2/")   // 🔥 Google OAuth 콜백
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/api/auth/")
                || uri.startsWith("/api/oauth/")
                || uri.startsWith("/error")
                || uri.startsWith("/swagger")
                || uri.startsWith("/v3/api-docs")
                || uri.equals("/")
                || uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                chain.doFilter(request, response);
                return;
            }

            String subject = jwtTokenProvider.getSubject(token);
            String[] parts = subject.split(":");

            if (parts.length != 2) {
                chain.doFilter(request, response);
                return;
            }

            OauthProvider provider = OauthProvider.valueOf(parts[0]);
            String oauthId = parts[1];

            User user = userRepository
                    .findByOauthProviderAndOauthId(provider, oauthId)
                    .orElse(null);

            if (user == null) {
                chain.doFilter(request, response);
                return;
            }

            CustomUserDetails cud = new CustomUserDetails(user);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            cud, null, cud.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            log.error("JWT filter error", e);
        }

        chain.doFilter(request, response);
    }
}