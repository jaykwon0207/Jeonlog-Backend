package com.jeonlog.exhibition_recommender.comment.repository;

import com.jeonlog.exhibition_recommender.comment.domain.RecordComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordCommentRepository extends JpaRepository<RecordComment, Long> {
    List<RecordComment> findByRecordIdAndParentIsNullOrderByCreatedAtAsc(Long recordId);
}

