package com.jeonlog.exhibition_recommender.user.repository;

import com.jeonlog.exhibition_recommender.user.domain.RecordLike;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordLikeRepository extends JpaRepository<RecordLike, Long> {
    boolean existsByUserAndRecord(User user, ExhibitionRecord record);
    Optional<RecordLike> findByUserAndRecord(User user, ExhibitionRecord record);
    List<RecordLike> findAllByUserOrderByLikedAtDesc(User user);
}
