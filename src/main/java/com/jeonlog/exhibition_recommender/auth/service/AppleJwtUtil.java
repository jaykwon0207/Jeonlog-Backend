package com.jeonlog.exhibition_recommender.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class AppleJwtUtil {

    public static String createClientSecret(String clientId, String teamId, String keyId, String privateKeyPem) throws Exception {
        String cleanPem = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleanPem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = KeyFactory.getInstance("EC").generatePrivate(spec);

        Instant now = Instant.now();
        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setHeaderParam("alg", "ES256")
                .setIssuer(teamId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(1800))) // 30분
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }
}