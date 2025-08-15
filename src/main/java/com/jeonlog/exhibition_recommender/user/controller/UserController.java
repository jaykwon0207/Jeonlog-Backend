package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyInfo(@AuthenticationPrincipal String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ 사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(UserDto.from(user));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        return ResponseEntity.ok().body("✅ 로그아웃 처리 완료 (클라이언트 토큰 삭제 필요)");
    }


    @DeleteMapping
    public ResponseEntity<?> deleteUser(Authentication authentication) {
        userService.deleteCurrentUser(authentication);
        return ResponseEntity.ok().body("✅ 회원 탈퇴 완료");
    }

    // 회원정보 수정
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMyInfo(
            @AuthenticationPrincipal String email,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserInfo(email, request));
    }

    // 닉네임 중복체크
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        return ResponseEntity.ok(isDuplicate);
    }
}