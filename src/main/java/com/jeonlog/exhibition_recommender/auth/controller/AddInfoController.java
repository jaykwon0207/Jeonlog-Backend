package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class AddInfoController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 신규 사용자 추가 정보 입력 + 회원가입 완료 처리
     */
    @PostMapping("/add-info")
    public ResponseEntity<ApiResponse<Map<String, String>>> completeSignUp(
            @RequestBody AddInfoRequestDto request,
            HttpSession session,
            HttpServletResponse response
    ) {
        // 1️⃣ 세션에서 OAuth 임시 정보 가져오기
        OAuthAttributes tempAttributes = (OAuthAttributes) session.getAttribute("tempOAuthAttributes");
        if (tempAttributes == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("SESSION_EXPIRED", "세션이 만료되었습니다. 다시 로그인해주세요."));
        }

        // 2️⃣ 닉네임 중복 검사
        if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_NICKNAME", "닉네임은 필수 입력 항목입니다."));
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            return ResponseEntity.status(409)
                    .body(ApiResponse.error("DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."));
        }

        // 3️⃣ 신규 사용자 저장
        User user = userService.saveNewUser(tempAttributes, request);

        // 4️⃣ 세션 정리
        session.removeAttribute("tempOAuthAttributes");

        // 5️⃣ JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 6️⃣ refresh_token → HttpOnly 쿠키 저장 (웹용)
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서는 true 유지
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // 7️⃣ JSON 응답 (모바일용)
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return ResponseEntity.ok(ApiResponse.ok(tokens));
    }
}