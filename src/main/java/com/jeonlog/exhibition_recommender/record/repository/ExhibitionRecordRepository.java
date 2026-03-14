package com.jeonlog.exhibition_recommender.record.repository;

import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExhibitionRecordRepository
        extends JpaRepository<ExhibitionRecord, Long>, ExhibitionRecordRepositoryCustom {

    // 조회용 (유지)
    List<ExhibitionRecord> findAllByUserOrderByCreatedAtDesc(User user);
    int countByUser(User user);

    Page<ExhibitionRecord> findByExhibitionId(Long exhibitionId, Pageable pageable);
    Page<ExhibitionRecord> findByHashtags_Name(String hashtagName, Pageable pageable);

    @EntityGraph(attributePaths = {
            "mediaList", "hashtags", "exhibition", "exhibition.venue", "user"
    })
    Optional<ExhibitionRecord> findWithDetailById(Long recordId);

    @Override
    @EntityGraph(attributePaths = {
            "mediaList", "hashtags", "exhibition", "user"
    })
    Page<ExhibitionRecord> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
            "mediaList", "hashtags", "exhibition", "user"
    })
    Page<ExhibitionRecord> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // ✅ 탈퇴 전용 bulk delete
    void deleteAllByUser(User user);

    // ✅ 탈퇴 전 좋아요 삭제용 (ID만 필요할 때 대비)
    @Query("select r.id from ExhibitionRecord r where r.user = :user")
    List<Long> findIdsByUser(@Param("user") User user);
}
