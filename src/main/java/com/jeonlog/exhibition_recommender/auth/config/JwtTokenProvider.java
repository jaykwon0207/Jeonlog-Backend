package com.jeonlog.exhibition_recommender.auth.config;

import com.jeonlog.exhibition_recommender.user.domain.User;
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

    private final long accessTokenValidityMs = 15 * 60 * 1000;
    private final long refreshTokenValidityMs = 14 * 24 * 60 * 60 * 1000;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // ✅ 공통 Subject 생성
    private String buildSubject(User user) {
        return user.getOauthProvider().name() + ":" + user.getOauthId();
    }

    // ✅ Access Token
    public String createAccessToken(User user) {
        return Jwts.builder()
                .setSubject(buildSubject(user))
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + accessTokenValidityMs)
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Refresh Token (User 기준으로 통일)
    public String createRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(buildSubject(user))
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + refreshTokenValidityMs)
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Subject 추출
    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ ROLE 추출
    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    // ✅ 토큰 유효성
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // ✅ Refresh → Access 재발급
    public String refreshAccessToken(String refreshToken, User user) {
        validateToken(refreshToken);
        return createAccessToken(user);
    }

    // ✅ Temp Token (온보딩)
    public String createTempToken(String base64Attributes, long validityMs) {
        return Jwts.builder()
                .setSubject("TEMP")
                .claim("data", base64Attributes)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + validityMs)
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Temp Token decode
    public String getDataFromTempToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("data", String.class);
    }
}