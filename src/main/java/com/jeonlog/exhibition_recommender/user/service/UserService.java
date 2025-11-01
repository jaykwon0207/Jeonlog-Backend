package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 🔹 내 정보 조회
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        return UserDto.from(user);
    }

    // 🔹 회원 탈퇴
    @Transactional
    public void deleteCurrentUser(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        userRepository.deleteByEmail(email);
    }

    // 🔹 회원 정보 수정
    @Transactional
    public UserDto updateUserInfo(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자를 찾을 수 없습니다."));

        // 닉네임 중복 검사
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.updateNickname(request.getNickname());
        }

        if (request.getIntroduction() != null) {
            user.updateIntroduction(request.getIntroduction());
        }

        if (request.getProfileImageUrl() != null) {
            user.updateProfileImageUrl(request.getProfileImageUrl());
        }

        return UserDto.from(user);
    }

    // 🔹 추가정보 업데이트
    @Transactional
    public void updateExtraInfo(String email, AddInfoRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }

        user.updateExtraInfo(dto.getGender(), dto.getBirthYear(), dto.getNickname());
    }

    @Transactional
    public void deleteCurrentUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }



}