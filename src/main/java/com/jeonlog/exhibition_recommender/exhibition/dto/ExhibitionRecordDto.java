package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.MediaType;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ExhibitionRecordDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @Size(max = 3000, message = "content는 최대 3000자입니다")
        private String content;

        private List<String> photoUrls;

        private String videoUrl;
        private Integer videoDurationSeconds;
        private String videoThumbnailUrl;

        private Boolean draft;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateResponse {
        private String message;
        private Long recordId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyRecordSummary {
        private Long id;
        private Long exhibitionId;
        private String content;
        private Boolean draft;
        private Long likeCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<MediaItem> media;

        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class MediaItem {
            private Long id;
            private MediaType type;
            private String fileUrl;
            private String thumbnailUrl;     // PHOTO일 땐 null
            private Integer durationSeconds; // PHOTO일 땐 null
        }
    }

}
