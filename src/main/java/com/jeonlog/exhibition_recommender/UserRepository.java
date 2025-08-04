package com.jeonlog.exhibition_recommender;

import com.jeonlog.exhibition_recommender.domain.user.OauthProvider;
import com.jeonlog.exhibition_recommender.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndOauthProvider(String email, OauthProvider oauthProvider);
}
