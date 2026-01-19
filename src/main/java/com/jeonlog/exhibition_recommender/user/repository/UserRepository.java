package com.jeonlog.exhibition_recommender.user.repository;

import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 기반 조회
    Optional<User> findByEmail(String email);



    // 닉네임 중복 여부 확인
    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    Optional<User> findByOauthIdAndOauthProvider(String oauthId, OauthProvider provider);


    // 닉네임으로 검색 (대소문자 구분 없이, 포함 검색)
    Page<User> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);


    Optional<User> findByOauthProviderAndOauthId(
            OauthProvider oauthProvider,
            String oauthId
    );

}