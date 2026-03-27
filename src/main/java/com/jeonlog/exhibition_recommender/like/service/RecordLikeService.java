// like/service/RecordLikeService.java
package com.jeonlog.exhibition_recommender.like.service;

import com.jeonlog.exhibition_recommender.like.domain.RecordLike;
import com.jeonlog.exhibition_recommender.like.dto.RecordLikeDto;
import com.jeonlog.exhibition_recommender.like.repository.RecordLikeRepository;
import com.jeonlog.exhibition_recommender.notification.service.NotificationService;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.domain.RecordMedia;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordLikeService {

    private final RecordLikeRepository recordLikeRepository;
    private final ExhibitionRecordRepository exhibitionRecordRepository;
    private final NotificationService notificationService;
    private final UserGenreRepository userGenreRepository;

    @Transactional
    public RecordLikeDto like(Long recordId, User user) {
        ExhibitionRecord record = exhibitionRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("전시기록을 찾을 수 없습니다."));

        RecordLike like = recordLikeRepository.findByUserAndRecord(user, record)
                .orElseGet(() -> {
                    RecordLike newLike = RecordLike.builder()
                            .user(user)
                            .record(record)
                            .build();

                    recordLikeRepository.save(newLike);
                    record.increaseLikeCount();

                    if (record.getExhibition() != null) {
                        UserGenre ug = getOrCreateUserGenre(user.getId());
                        ug.addFromRecordLike(record.getExhibition().getGenre(), record.getExhibition().getExhibitionTheme());
                    }

                    notificationService.notifyRecordLike(
                            record.getId(),
                            record.getUser().getId(),
                            user.getId(),
                            user.getNickname()
                    );

                    return newLike;
                });

        return toDto(record, like.getLikedAt(), true);
    }

    @Transactional
    public RecordLikeDto unlike(Long recordId, User user) {
        ExhibitionRecord record = exhibitionRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("전시기록을 찾을 수 없습니다."));

        LocalDateTime likedAt = null;

        var optionalLike = recordLikeRepository.findByUserAndRecord(user, record);

        if (optionalLike.isPresent()) {
            RecordLike like = optionalLike.get();
            likedAt = like.getLikedAt();
            recordLikeRepository.delete(like);
            record.decreaseLikeCount();

            if (record.getExhibition() != null) {
                userGenreRepository.findByUserId(user.getId()).ifPresent(ug ->
                        ug.revertRecordLike(record.getExhibition().getGenre(), record.getExhibition().getExhibitionTheme())
                );
            }
        }

        return toDto(record, likedAt, false);
    }

    public List<RecordLikeDto> getMyLiked(User user) {
        return recordLikeRepository.findAllByUserOrderByLikedAtDesc(user).stream()
                .map(like -> toDto(like.getRecord(), like.getLikedAt(), true))
                .collect(Collectors.toList());
    }

    private RecordLikeDto toDto(ExhibitionRecord record, LocalDateTime likedAt, boolean liked) {
        return RecordLikeDto.builder()
                .recordId(record.getId())
                .exhibitionId(record.getExhibition().getId())
                .exhibitionTitle(record.getExhibition().getTitle())
                .contentPreview(record.getContent() != null && record.getContent().length() > 50
                        ? record.getContent().substring(0, 50) + "..."
                        : record.getContent())
                .likeCount(record.getLikeCount())
                .likedAt(likedAt)
                .media(record.getMediaList().stream()
                        .map(this::toMediaDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private RecordLikeDto.MediaItem toMediaDto(RecordMedia media) {
        return RecordLikeDto.MediaItem.builder()
                .id(media.getId())
                .type(media.getMediaType())
                .fileUrl(media.getFileUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .durationSeconds(media.getDurationSeconds())
                .build();
    }

    private UserGenre getOrCreateUserGenre(Long userId) {
        return userGenreRepository.findByUserId(userId)
                .orElseGet(() -> userGenreRepository.save(UserGenre.builder().userId(userId).build()));
    }
}
