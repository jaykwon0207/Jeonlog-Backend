package com.jeonlog.exhibition_recommender.auth.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${JWT_SECRET}")
    private String secretKey;

    private final long expirationMs = 3600_000; // 1시간

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String email) {
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("🔐 JWT 발급 완료: {}", token);
        return token;
    }

    public String getEmailFromToken(String token) {
        String email = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        log.info("📨 JWT에서 추출된 이메일: {}", email);
        return email;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            log.info("✅ JWT 유효성 검증 성공");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("❌ JWT 유효성 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}