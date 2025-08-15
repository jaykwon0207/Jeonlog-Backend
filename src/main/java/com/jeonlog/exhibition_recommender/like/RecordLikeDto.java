package com.jeonlog.exhibition_recommender.like;

import com.jeonlog.exhibition_recommender.exhibition.domain.MediaType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecordLikeDto {
    private Long recordId;
    private Long exhibitionId;
    private String content;         // 일부만 잘라서
    private Long likeCount;
    private Boolean draft;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime likedAt;  // 내가 좋아요한 시각
    private List<MediaItem> media;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MediaItem {
        private Long id;
        private MediaType type;
        private String fileUrl;
        private String thumbnailUrl;
        private Integer durationSeconds;
    }
}
