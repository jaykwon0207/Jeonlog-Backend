package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // ✅ 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<UserDto> getMyInfo(@CurrentUser User user) {
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        return ApiResponse.ok(UserDto.from(user));
    }

    // ✅ 회원정보 수정 (닉네임, 성별, 출생연도, 자기소개, 프로필이미지, 시그니처 포함)
    @PutMapping("/me")
    public ApiResponse<UserDto> updateMyInfo(
            @CurrentUser User user,
            @RequestBody UserUpdateRequest request
    ) {
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        return ApiResponse.ok(userService.updateUserInfo(user.getEmail(), request));
    }

    // ✅ 닉네임 중복체크
    @GetMapping("/check-nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam String nickname) {
        return ApiResponse.ok(userRepository.existsByNickname(nickname));
    }

    // ✅ 회원 탈퇴
    @DeleteMapping
    public ApiResponse<String> deleteUser(@CurrentUser User user) {
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        userService.deleteCurrentUserByEmail(user.getEmail());
        return ApiResponse.ok("회원 탈퇴 완료");
    }
}