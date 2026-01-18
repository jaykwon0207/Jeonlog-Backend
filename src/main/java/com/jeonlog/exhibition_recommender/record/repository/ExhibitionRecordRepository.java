package com.jeonlog.exhibition_recommender.record.repository;

import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExhibitionRecordRepository extends
        JpaRepository<ExhibitionRecord, Long>, ExhibitionRecordRepositoryCustom {
    List<ExhibitionRecord> findAllByUserOrderByCreatedAtDesc(User user);
    int countByUser(User user);

    Page<ExhibitionRecord> findByExhibitionId(Long exhibitionId, Pageable pageable);

    Page<ExhibitionRecord> findByHashtags_Name(String hashtagName, Pageable pageable);

    @EntityGraph(attributePaths = {"mediaList", "hashtags", "exhibition", "exhibition.venue", "user"})
    Optional<ExhibitionRecord> findWithDetailById(Long recordId);

    @EntityGraph(attributePaths = {"mediaList", "hashtags", "exhibition", "user"})
    Page<ExhibitionRecord> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

}
