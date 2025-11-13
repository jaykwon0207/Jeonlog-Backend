package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자를 찾을 수 없습니다."));
        return UserDto.from(user);
    }

    // ✅ 회원 정보 수정 (닉네임, 성별, 출생연도, 자기소개, 프로필이미지, 시그니처)
    @Transactional
    public UserDto updateUserInfo(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자를 찾을 수 없습니다."));

        // 닉네임 중복 체크
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }

        // ✅ 시그니처 포함하여 업데이트 호출
        user.updateProfile(
                request.getGender(),
                request.getBirthYear(),
                request.getNickname(),
                request.getIntroduction(),
                request.getProfileImageUrl(),
                request.getSignature()
        );

        return UserDto.from(user);
    }

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
                .nickname(request.getNickname())
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void deleteCurrentUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }

    @Transactional
    public UserDto updateSignature(String email, String signature) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자를 찾을 수 없습니다."));
        user.updateSignature(signature);
        return UserDto.from(user);
    }
}