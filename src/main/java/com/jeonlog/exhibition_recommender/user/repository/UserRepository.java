package com.jeonlog.exhibition_recommender.user.repository;

import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 기반 조회
    Optional<User> findByEmail(String email);



    // 닉네임 중복 여부 확인
    boolean existsByNickname(String nickname);




    // 닉네임으로 검색 (대소문자 구분 없이, 포함 검색)
    Page<User> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);

    boolean existsByOauthProviderAndOauthId(OauthProvider oauthProvider, String oauthId
    );

    Optional<User> findByOauthProviderAndOauthId(
            OauthProvider oauthProvider,
            String oauthId
    );

    // 알림용: actor 프사 url bulk 조회
    interface UserProfileImageProjection {
        Long getId();
        String getProfileImageUrl();
    }

    @Query("select u.id as id, u.profileImageUrl as profileImageUrl from User u where u.id in :ids")
    List<UserProfileImageProjection> findProfileImageUrlsByIds(@Param("ids") List<Long> ids);


}