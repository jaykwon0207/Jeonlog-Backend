package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.domain.UserLike;
import com.jeonlog.exhibition_recommender.user.dto.UserLikeDto;
import com.jeonlog.exhibition_recommender.user.repository.UserLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLikeService {

    private final UserLikeRepository likeRepository;
    private final ExhibitionRepository exhibitionRepository;

    public void addLike(Long exhibitionId, User user) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시가 존재하지 않습니다."));

        if (likeRepository.existsByUserAndExhibition(user, exhibition)) {
            throw new IllegalArgumentException("이미 좋아요한 전시입니다.");
        }

        UserLike like = UserLike.builder()
                .user(user)
                .exhibition(exhibition)
                .likedAt(LocalDateTime.now())
                .build();

        likeRepository.save(like);
    }

    public void cancelLike(Long exhibitionId, User user) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시가 존재하지 않습니다."));

        UserLike like = likeRepository.findByUserAndExhibition(user, exhibition)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 누른 적이 없습니다."));

        likeRepository.delete(like);
    }

    public List<UserLikeDto> getLikesByUser(User user) {
        return likeRepository.findAllByUser(user).stream()
                .map(like -> UserLikeDto.from(like.getExhibition()))
                .toList();
    }
}
