package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ 1. 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyInfo(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(userService.getMyInfo(email));
    }

    // ✅ 2. 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok().build();
    }

    // ✅ 3. 회원탈퇴
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }
}