package com.jeonlog.exhibition_recommender.like.repository;

import com.jeonlog.exhibition_recommender.like.domain.ExhibitionLike;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitionLikeRepository extends JpaRepository<ExhibitionLike, Long> {

    // 특정 유저가 특정 전시에 좋아요를 눌렀는지 확인
    boolean existsByUserIdAndExhibitionId(Long userId, Long exhibitionId);

    // 특정 유저의 특정 전시에 대한 좋아요 삭제
    void deleteByUserIdAndExhibitionId(Long userId, Long exhibitionId);

    // 특정 전시에 좋아요 수 카운트
    long countByExhibitionId(Long exhibitionId);

    // 특정 전시에 좋아요한 사용자 목록 (페이징)
    Page<ExhibitionLike> findByExhibitionId(Long exhibitionId, Pageable pageable);

    // 특정 유저가 좋아요한 전시 목록 (페이징)
    Page<ExhibitionLike> findByUser(User user, Pageable pageable);
}