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
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("[AUTH] jwt_filter_skipped reason=missing_or_invalid_header uri={} method={}", uri, method);
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("[AUTH] jwt_filter_failed reason=invalid_token uri={} method={}", uri, method);
                chain.doFilter(request, response);
                return;
            }

            String subject = jwtTokenProvider.getSubject(token);
            String[] parts = subject.split(":");

            if (parts.length != 2) {
                log.warn("[AUTH] jwt_filter_failed reason=invalid_subject_format uri={} method={}", uri, method);
                chain.doFilter(request, response);
                return;
            }

            OauthProvider provider = OauthProvider.valueOf(parts[0]);
            String oauthId = parts[1];

            User user = userRepository
                    .findByOauthProviderAndOauthId(provider, oauthId)
                    .orElse(null);

            if (user == null) {
                log.warn(
                        "[AUTH] jwt_filter_failed reason=user_not_found provider={} uri={} method={}",
                        provider,
                        uri,
                        method
                );
                chain.doFilter(request, response);
                return;
            }

            CustomUserDetails cud = new CustomUserDetails(user);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            cud, null, cud.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug(
                    "[AUTH] jwt_filter_authenticated userId={} provider={} uri={} method={}",
                    user.getId(),
                    provider,
                    uri,
                    method
            );

        } catch (Exception e) {
            log.error("[AUTH] jwt_filter_error uri={} method={} reason={}", uri, method, e.getMessage(), e);
        }

        chain.doFilter(request, response);
    }
}
