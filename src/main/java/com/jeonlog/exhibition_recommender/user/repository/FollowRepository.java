package com.jeonlog.exhibition_recommender.user.repository;

import com.jeonlog.exhibition_recommender.user.domain.Follow;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowing(User follower, User following);
    List<Follow> findByFollower(User follower);
    List<Follow> findByFollowing(User following);
    int countByFollower(User user);
    int countByFollowing(User user);
    void deleteByFollowerAndFollowing(User follower, User following);
    void deleteAllByFollowerOrFollowing(User follower, User following);
    @Query("select f.following.id from Follow f where f.follower = :follower")
    List<Long> findFollowingIdsByFollower(@Param("follower") User follower);
}