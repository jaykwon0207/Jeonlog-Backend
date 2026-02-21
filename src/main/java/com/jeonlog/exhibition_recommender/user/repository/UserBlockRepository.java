package com.jeonlog.exhibition_recommender.user.repository;

import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    void deleteByBlockerAndBlocked(User blocker, User blocked);

    void deleteAllByBlockerOrBlocked(User blocker, User blocked);

    @Query("select b.blocked.id from UserBlock b where b.blocker.id = :userId")
    Set<Long> findBlockedUserIdsByBlockerId(@Param("userId") Long userId);

    @Query("select b.blocker.id from UserBlock b where b.blocked.id = :userId")
    Set<Long> findBlockerUserIdsByBlockedId(@Param("userId") Long userId);

    @Query("""
            select case when count(b) > 0 then true else false end
            from UserBlock b
            where (b.blocker.id = :userId and b.blocked.id = :targetId)
               or (b.blocker.id = :targetId and b.blocked.id = :userId)
            """)
    boolean existsRelationBetween(@Param("userId") Long userId, @Param("targetId") Long targetId);

    @Query("""
            select b
            from UserBlock b
            join fetch b.blocked
            where b.blocker = :blocker
            order by b.createdAt desc
            """)
    List<UserBlock> findByBlockerWithBlocked(@Param("blocker") User blocker);
}
