package com.jeonlog.exhibition_recommender.user.repository;

import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 기반 조회
    Optional<User> findByEmail(String email);

    // 이메일 기반 삭제
    void deleteByEmail(String email);

    // 닉네임 중복 여부 확인
    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);
}