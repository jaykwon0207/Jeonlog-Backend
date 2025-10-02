package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/add-info")
    public ApiResponse<Map<String, String>> addInfoSubmit(
            @RequestBody AddInfoRequestDto request,
            HttpSession session,
            HttpServletResponse response
    ) {
        // 1. 세션에서 임시 OAuth 정보 꺼내오기
        OAuthAttributes tempAttr = (OAuthAttributes) session.getAttribute("tempOAuthAttributes");
        if (tempAttr == null) {
            return ApiResponse.error("SESSION_EXPIRED", "세션 만료 또는 잘못된 접근입니다.");
        }

        // 2. 닉네임 중복 체크
        if (userRepository.existsByNickname(request.getNickname())) {
            return ApiResponse.error("DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.");
        }

        // 3. User 생성 및 저장
        User user = User.builder()
                .name(tempAttr.getName())
                .email(tempAttr.getEmail())
                .oauthProvider(tempAttr.getOauthProvider())
                .oauthId(tempAttr.getOauthId())
                .gender(request.getGender())
                .birthYear(request.getBirthYear())
                .nickname(request.getNickname())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        session.removeAttribute("tempOAuthAttributes");

        // 4. JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 5. Refresh Token → HttpOnly 쿠키 저장
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // 6. 응답 JSON 반환 (프론트에서 accessToken 저장 가능)
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", accessToken);

        return ApiResponse.ok(result);
    }

}