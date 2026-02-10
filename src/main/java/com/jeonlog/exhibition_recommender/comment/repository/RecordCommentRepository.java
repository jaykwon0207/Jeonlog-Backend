package com.jeonlog.exhibition_recommender.comment.repository;

import com.jeonlog.exhibition_recommender.comment.domain.RecordComment;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordCommentRepository extends JpaRepository<RecordComment, Long> {
    List<RecordComment> findByRecordIdAndParentIsNullOrderByCreatedAtAsc(Long recordId);

    void deleteAllByRecordIn(List<ExhibitionRecord> records);
}

