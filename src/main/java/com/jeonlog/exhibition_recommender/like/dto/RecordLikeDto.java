package com.jeonlog.exhibition_recommender.like.dto;

import com.jeonlog.exhibition_recommender.record.domain.MediaType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecordLikeDto {
    private Long recordId;
    private Long exhibitionId;
    private String exhibitionTitle;
    private String contentPreview;
    private Long likeCount;
    private LocalDateTime likedAt;
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