package com.jeonlog.exhibition_recommender.like;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
    boolean existsByUserAndExhibition(User user, Exhibition exhibition);

    Optional<UserLike> findByUserAndExhibition(User user, Exhibition exhibition);

    List<UserLike> findAllByUser(User user);

}
