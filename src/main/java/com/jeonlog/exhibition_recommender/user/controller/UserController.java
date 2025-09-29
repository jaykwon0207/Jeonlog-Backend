package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // 🔹 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<UserDto> getMyInfo(@AuthenticationPrincipal String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return ApiResponse.ok(UserDto.from(user));
    }

    // 🔹 로그아웃
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        // 실제 토큰 무효화 로직은 없고, 클라이언트에서 삭제 필요
        return ApiResponse.ok("✅ 로그아웃 처리 완료 (클라이언트 토큰 삭제 필요)");
    }

    // 🔹 회원 탈퇴
    @DeleteMapping
    public ApiResponse<String> deleteUser(Authentication authentication) {
        userService.deleteCurrentUser(authentication);
        return ApiResponse.ok("✅ 회원 탈퇴 완료");
    }

    // 🔹 회원정보 수정
    @PutMapping("/me")
    public ApiResponse<UserDto> updateMyInfo(
            @AuthenticationPrincipal String email,
            @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(userService.updateUserInfo(email, request));
    }

    // 🔹 닉네임 중복체크
    @GetMapping("/check-nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        return ApiResponse.ok(isDuplicate);
    }

    @PostMapping("/add-info")
    public ResponseEntity<ApiResponse<?>> addInfo(
            @RequestBody AddInfoRequestDto dto,
            @RequestAttribute("email") String email) {

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