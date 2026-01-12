package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;

    @Transactional
    public UserDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return UserDto.from(user);
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

        User saved = userRepository.save(user);

        userGenreRepository.save(
                UserGenre.builder()
                        .userId(saved.getId())
                        .build()
        );

        return saved;
    }

    @Transactional
    public UserDto updateUserInfo(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();

        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }

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
    public void deleteCurrentUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        userRepository.delete(user);
    }

    @Transactional
    public UserDto updateSignature(String email, String signature) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.updateSignature(signature);
        return UserDto.from(user);
    }

    public Page<UserDto.UserSearchResponse> searchUsersByNickname(String nickname, Pageable pageable) {
        if (nickname == null || nickname.isBlank()) {
            // 검색어가 없을 경우 빈 페이지 반환
            return Page.empty(pageable);
        }

        return userRepository.findByNicknameContainingIgnoreCase(nickname, pageable)
                .map(UserDto.UserSearchResponse::of);
    }

    //유저 상세조회
    @Transactional(readOnly = true)
    public UserDto.UserDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        return UserDto.UserDetailResponse.from(user);
    }

}