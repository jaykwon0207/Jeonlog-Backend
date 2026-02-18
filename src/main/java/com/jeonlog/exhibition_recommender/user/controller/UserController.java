package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserOnboardingRequest;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserService;
import com.jeonlog.exhibition_recommender.user.service.UserWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserWithdrawService userWithdrawService;


    // ✅ 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<UserDto> getMyInfo(@CurrentUser User user) {
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        return ApiResponse.ok(UserDto.from(user));
    }

    // ✅ 🆕 온보딩 완료
    @PostMapping("/onboarding")
    public ApiResponse<UserDto> completeOnboarding(
            @CurrentUser User user,
            @RequestBody UserOnboardingRequest request
    ) {
        return ApiResponse.ok(userService.completeOnboarding(user, request));
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
        userWithdrawService.withdraw(user.getId());
        return ApiResponse.ok("회원 탈퇴 완료");
    }

    @GetMapping("/search") // 유저 검색
    public ApiResponse<Page<UserDto.UserSearchResponse>> searchUsers(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "nickname", required = false) String nickname,
            @PageableDefault(size = 10, sort = "nickname", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        String keyword = query != null ? query : nickname;
        Page<UserDto.UserSearchResponse> users = userService.searchUsersByNickname(keyword, pageable);
        return ApiResponse.ok(users);
    }

    //  유저 상세 조회 (프로필 조회)
    @GetMapping("/{userId}")
    public ApiResponse<UserDto.UserDetailResponse> getUserDetail(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUserDetail(userId));
    }


}
