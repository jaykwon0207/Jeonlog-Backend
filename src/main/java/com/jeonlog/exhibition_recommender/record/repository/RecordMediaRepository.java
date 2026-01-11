package com.jeonlog.exhibition_recommender.record.repository;

import com.jeonlog.exhibition_recommender.record.domain.RecordMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordMediaRepository extends JpaRepository<RecordMedia, Long> {
    List<RecordMedia> findAllByRecordId(Long recordId);
    void deleteAllByRecordId(Long recordId);
}