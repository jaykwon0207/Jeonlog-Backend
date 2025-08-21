package com.jeonlog.exhibition_recommender.record.repository;

import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExhibitionRecordRepository extends JpaRepository<ExhibitionRecord, Long> {
    List<ExhibitionRecord> findAllByUserOrderByCreatedAtDesc(User user);
    int countByUser(User user);
}
