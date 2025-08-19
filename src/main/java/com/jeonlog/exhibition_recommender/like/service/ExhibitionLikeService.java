package com.jeonlog.exhibition_recommender.like.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.like.domain.ExhibitionLike;
import com.jeonlog.exhibition_recommender.like.dto.LikeUserDto;
import com.jeonlog.exhibition_recommender.like.repository.ExhibitionLikeRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExhibitionLikeService {

    private final ExhibitionLikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ExhibitionRepository exhibitionRepository;

    // 좋아요 추가
    @Transactional
    public void like(Long exhibitionId, String email) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));

        // 이미 눌렀으면 중복 방지
        if (likeRepository.existsByUserIdAndExhibitionId(me.getId(), ex.getId())) return;

        likeRepository.save(ExhibitionLike.builder()
                .user(me)
                .exhibition(ex)
                .build());

        // (선택) 전시 엔티티에 likeCount 필드 두고 증가 처리 가능
    }

    // 좋아요 취소
    @Transactional
    public void unlike(Long exhibitionId, String email) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        likeRepository.deleteByUserIdAndExhibitionId(me.getId(), exhibitionId);

        // (선택) 전시 엔티티에 likeCount 필드 두고 감소 처리 가능
    }

    // 좋아요 수 조회
    @Transactional(readOnly = true)
    public long count(Long exhibitionId) {
        return likeRepository.countByExhibitionId(exhibitionId);
    }

    // 전시에 좋아요 누른 유저 목록 조회
    @Transactional(readOnly = true)
    public Page<LikeUserDto> listUsers(Long exhibitionId, Pageable pageable) {
        return likeRepository.findByExhibitionId(exhibitionId, pageable)
                .map(like -> LikeUserDto.from(like.getUser()));
    }

    // 내가 좋아요한 전시 목록 조회
    @Transactional(readOnly = true)
    public Page<Exhibition> listMyLikedExhibitions(String email, Pageable pageable) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return likeRepository.findByUser(me, pageable)
                .map(ExhibitionLike::getExhibition);
    }
}