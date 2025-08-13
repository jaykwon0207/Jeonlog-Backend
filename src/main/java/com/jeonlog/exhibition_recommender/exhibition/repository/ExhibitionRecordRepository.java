package com.jeonlog.exhibition_recommender.exhibition.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExhibitionRecordRepository extends JpaRepository<ExhibitionRecord, Long> {
    List<ExhibitionRecord> findAllByUserOrderByCreatedAtDesc(User user);
}
