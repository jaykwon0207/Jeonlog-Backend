package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.bookmark.repository.BookmarkRepository;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserDto;
import com.jeonlog.exhibition_recommender.user.dto.UserOnboardingRequest;
import com.jeonlog.exhibition_recommender.user.dto.UserUpdateRequest;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;
    private final BookmarkRepository bookmarkRepository;

    // =========================
    // 1️⃣ 온보딩 완료 (신규 사용자)
    // =========================
    public UserDto completeOnboarding(User user, UserOnboardingRequest request) {

        if (user.getGender() != null || user.getBirthYear() != null) {
            throw new IllegalStateException("이미 온보딩이 완료된 사용자입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        user.completeOnboarding(
                request.getGender(),
                request.getBirthYear(),
                request.getNickname()
        );

        return UserDto.from(user);
    }

    // =========================
    // 2️⃣ 내 정보 조회
    // =========================
    @Transactional(readOnly = true)
    public UserDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return UserDto.from(user);
    }

    // =========================
    // 3️⃣ (구 OAuth2) 신규 유저 저장
    // ※ 기존 구조 유지용
    // =========================
    public User saveNewUser(OAuthAttributes attributes, AddInfoRequestDto request) {

        User user = User.builder()
                .email(attributes.getEmail())
                .name(attributes.getName())
                .oauthProvider(attributes.getOauthProvider())
                .oauthId(attributes.getOauthId())
                .gender(request.getGender())
                .birthYear(request.getBirthYear())
                .nickname(request.getNickname())
                .build();

        User savedUser = userRepository.save(user);

        userGenreRepository.save(
                UserGenre.builder()
                        .userId(savedUser.getId())
                        .build()
        );

        return savedUser;
    }

    // =========================
    // 4️⃣ 회원정보 수정 (프로필 수정)
    // =========================
    public UserDto updateUserInfo(String email, UserUpdateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.getNickname() != null &&
                !request.getNickname().equals(user.getNickname()) &&
                userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
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
    public void deleteCurrentUser(User user) {
        bookmarkRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }


    @Transactional(readOnly = true)
    public Page<UserDto.UserSearchResponse> searchUsersByNickname(
            String nickname,
            Pageable pageable
    ) {
        if (nickname == null || nickname.isBlank()) {
            return Page.empty(pageable);
        }

        String normalizedNickname = nickname.trim();

        return userRepository
                .findByNicknameContainingIgnoreCase(normalizedNickname, pageable)
                .map(UserDto.UserSearchResponse::of);
    }


    @Transactional(readOnly = true)
    public UserDto.UserDetailResponse getUserDetail(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        return UserDto.UserDetailResponse.from(user);
    }
}
