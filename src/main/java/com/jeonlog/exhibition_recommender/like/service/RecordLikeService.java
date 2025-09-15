package com.jeonlog.exhibition_recommender.like.service;

import com.jeonlog.exhibition_recommender.like.domain.RecordLike;
import com.jeonlog.exhibition_recommender.like.dto.RecordLikeDto;
import com.jeonlog.exhibition_recommender.like.repository.RecordLikeRepository;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordLikeService {

    private final RecordLikeRepository recordLikeRepository;
    private final ExhibitionRecordRepository recordRepository;
    private final UserGenreRepository userGenreRepository;

    //좋아요 생성, 가중치 +0.01
    @Transactional
    public void like(Long recordId, User user) {
        ExhibitionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("전시기록이 존재하지 않습니다."));

        if (recordLikeRepository.existsByUserAndRecord(user, record)) {
            throw new IllegalArgumentException("이미 좋아요한 전시기록입니다.");
        }

        recordLikeRepository.save(RecordLike.builder()
                .user(user)
                .record(record)
                .build());

        record.increaseLikeCount();

        UserGenre ug = userGenreRepository.findByUserId(user.getId())
                .orElseGet(() -> userGenreRepository.save(
                        UserGenre.builder().userId(user.getId()).build()
                ));

        ug.addFromRecordLike(
                record.getExhibition().getGenre(),
                record.getExhibition().getExhibitionTheme()
        );
    }

    //좋아요 취소, 가중치 -0.01
    @Transactional
    public void unlike(Long recordId, User user) {
        ExhibitionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("전시기록이 존재하지 않습니다."));

        RecordLike like = recordLikeRepository.findByUserAndRecord(user, record)
                .orElseThrow(() -> new IllegalArgumentException("좋아요하지 않은 전시기록입니다."));

        recordLikeRepository.delete(like);

        record.decreaseLikeCount();

        //가중치 감소 (−0.01)
        userGenreRepository.findByUserId(user.getId()).ifPresent(ug ->
                ug.revertRecordLike(
                        record.getExhibition().getGenre(),
                        record.getExhibition().getExhibitionTheme()
                )
        );
    }

    @Transactional(readOnly = true)
    public List<RecordLikeDto> getMyLiked(User user) {
        List<RecordLike> likes = recordLikeRepository.findAllByUserOrderByLikedAtDesc(user);
        return likes.stream().map(l -> {
            ExhibitionRecord r = l.getRecord();
            return RecordLikeDto.builder()
                    .recordId(r.getId())
                    .exhibitionId(r.getExhibition().getId())
                    .content(trim(r.getContent(), 200))
                    .likeCount(r.getLikeCount())
                    .createdAt(r.getCreatedAt())
                    .updatedAt(r.getUpdateAt())
                    .likedAt(l.getLikedAt())
                    .media(r.getMediaList().stream().map(m ->
                            RecordLikeDto.MediaItem.builder()
                                    .id(m.getId())
                                    .type(m.getMediaType())
                                    .fileUrl(m.getFileUrl())
                                    .thumbnailUrl(m.getThumbnailUrl())
                                    .durationSeconds(m.getDurationSeconds())
                                    .build()
                    ).toList())
                    .build();
        }).toList();
    }

    private String trim(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
