package com.jeonlog.exhibition_recommender.auth.config;

import com.jeonlog.exhibition_recommender.auth.model.CustomUserDetails; // ★ 추가
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                if (email != null) {
                    userRepository.findByEmail(email).ifPresent(user -> {
                        CustomUserDetails cud = new CustomUserDetails(user); // ★ 래핑
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(cud, null, cud.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
            }
        }


        chain.doFilter(request, response);

    }
}