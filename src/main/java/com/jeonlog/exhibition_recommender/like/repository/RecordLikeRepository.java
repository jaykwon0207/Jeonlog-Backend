package com.jeonlog.exhibition_recommender.like.repository;

import com.jeonlog.exhibition_recommender.like.domain.RecordLike;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecordLikeRepository extends JpaRepository<RecordLike, Long> {
    Optional<RecordLike> findByUserAndRecord(User user, ExhibitionRecord record);
    List<RecordLike> findAllByUserOrderByLikedAtDesc(User user);


    // ✅ 유저가 누른 좋아요
    void deleteAllByUser(User user);

    // ✅ 기록에 달린 좋아요
    void deleteAllByRecord(ExhibitionRecord record);

    void deleteAllByRecordIn(List<ExhibitionRecord> records);
}
