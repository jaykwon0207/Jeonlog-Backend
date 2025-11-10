package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserSignatureUpdateRequest;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // 🔹 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<UserDto> getMyInfo(@CurrentUser User user) {
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        return ApiResponse.ok(UserDto.from(user));
    }

    // 🔹 회원정보 수정
    @PutMapping("/me")
    public ApiResponse<UserDto> updateMyInfo(@CurrentUser User user,
                                             @RequestBody UserUpdateRequest request) {
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        return ApiResponse.ok(userService.updateUserInfo(user.getEmail(), request));
    }

    // 🔹 닉네임 중복체크
    @GetMapping("/check-nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam String nickname) {
        return ApiResponse.ok(userRepository.existsByNickname(nickname));
    }

    // 🔹 회원 탈퇴
    @DeleteMapping
    public ApiResponse<String> deleteUser(@AuthenticationPrincipal(expression = "user") User user) {
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        userService.deleteCurrentUserByEmail(user.getEmail());
        return ApiResponse.ok("✅ 회원 탈퇴 완료");
    }

    // 🔹 추가 정보 저장 (신규 회원가입)
    @PostMapping("/add-info")
    public ResponseEntity<ApiResponse<?>> addInfo(
            HttpServletRequest request,
            @RequestBody AddInfoRequestDto dto
    ) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NO_SESSION", "세션이 만료되었거나 존재하지 않습니다."));
        }

        OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("tempOAuthAttributes");
        if (attributes == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NO_OAUTH_ATTRIBUTES", "OAuth 임시 정보가 없습니다."));
        }

        try {
            userService.createNewUser(attributes, dto);
            session.removeAttribute("tempOAuthAttributes");
            return ResponseEntity.ok(ApiResponse.ok("신규 회원가입 완료"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("DUPLICATE_NICKNAME", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("SIGNUP_FAILED", e.getMessage()));
        }
    }

    //시그니처 수정
    @PutMapping("/signature")
    public ApiResponse<UserDto> updateSignature(@CurrentUser User user,
                                                @RequestBody UserSignatureUpdateRequest request) {
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        return ApiResponse.ok(userService.updateSignature(user.getEmail(), request.getSignature()));
    }


}