package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ✅ 1. 내 정보 조회
    public UserDto getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return UserDto.from(user);
    }

    // ✅ 2. 로그아웃 (실제로는 클라이언트에서 처리하므로 로직 없음)
    public void logout(HttpServletRequest request) {
        // JWT 기반에서는 별도 서버 측 처리가 필요 없는 경우가 일반적
    }

    // ✅ 3. 회원탈퇴
    public void deleteUser(String email) {
        userRepository.deleteByEmail(email);
    }
}