package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.auth.model.CustomUserDetails; // ★ 추가
import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ApiResponse<UserDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        User user = principal.getUser();
        return ApiResponse.ok(UserDto.from(user));
    }

    // 🔹 로그아웃
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        return ApiResponse.ok("✅ 로그아웃 처리 완료 (클라이언트 토큰 삭제 필요)");
    }

    // 🔹 회원 탈퇴
    @DeleteMapping
    public ApiResponse<String> deleteUser(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        userService.deleteCurrentUserByEmail(principal.getUser().getEmail()); // 서비스 시그니처에 맞게 사용
        return ApiResponse.ok("✅ 회원 탈퇴 완료");
    }

    // 🔹 회원정보 수정
    @PutMapping("/me")
    public ApiResponse<UserDto> updateMyInfo(@AuthenticationPrincipal CustomUserDetails principal,
                                             @RequestBody UserUpdateRequest request) {
        if (principal == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        String email = principal.getUser().getEmail();
        return ApiResponse.ok(userService.updateUserInfo(email, request));
    }

    // 🔹 닉네임 중복체크
    @GetMapping("/check-nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        return ApiResponse.ok(isDuplicate);
    }

    // 🔹 추가 정보 저장 (기존 @RequestAttribute("email") 제거하고 인증 주체 사용)
    @PostMapping("/add-info")
    public ResponseEntity<ApiResponse<?>> addInfo(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @RequestBody AddInfoRequestDto dto) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다.")
            );
        }
        String email = principal.getUser().getEmail();

        try {
            userService.updateExtraInfo(email, dto);
            return ResponseEntity.ok(ApiResponse.ok("추가 정보 저장 완료"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("DUPLICATE_NICKNAME", e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("USER_NOT_FOUND", e.getMessage())
            );
        }
    }
}