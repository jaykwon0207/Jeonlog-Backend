package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 🔹 회원 정보 수정
    @Transactional
    public UserDto updateUserInfo(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자를 찾을 수 없습니다."));

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

    // 🔹 신규 회원가입 (OAuth + 추가정보)
    @Transactional
    public void createNewUser(OAuthAttributes attributes, AddInfoRequestDto dto) {
        if (userRepository.existsByEmail(attributes.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .email(attributes.getEmail())
                .name(attributes.getName())
                .oauthProvider(attributes.getOauthProvider())
                .oauthId(attributes.getOauthId())
                .gender(dto.getGender())
                .birthYear(dto.getBirthYear())
                .nickname(dto.getNickname())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }


    @Transactional
    public User saveNewUser(OAuthAttributes attributes, AddInfoRequestDto request) {
        User user = User.builder()
                .email(attributes.getEmail())
                .name(attributes.getName())
                .oauthProvider(attributes.getOauthProvider())
                .oauthId(attributes.getOauthId())
                .gender(request.getGender())
                .birthYear(request.getBirthYear())
                .build();

        return userRepository.save(user);
    }

    // 🔹 회원 탈퇴
    @Transactional
    public void deleteCurrentUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
}