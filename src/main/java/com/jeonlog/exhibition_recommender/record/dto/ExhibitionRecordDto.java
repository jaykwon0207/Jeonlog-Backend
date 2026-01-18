package com.jeonlog.exhibition_recommender.record.dto;

import com.jeonlog.exhibition_recommender.record.domain.MediaType;
import com.jeonlog.exhibition_recommender.record.domain.RecordMedia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class ExhibitionRecordDto {

    // -------------------
    // 요청 DTO
    // -------------------

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "title은 필수입니다")
        @Size(max = 100, message = "title은 최대 100자입니다")
        private String title;

        @Size(max = 3000, message = "content는 최대 3000자입니다")
        private String content;

        private List<String> photoUrls;
        private String videoUrl;
        private Integer videoDurationSeconds;
        private String videoThumbnailUrl;

        private Set<@Size(max = 10, message = "해시태그는 10자 이하만 가능합니다") String> hashtags;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {

        @Size(max = 100, message = "title은 최대 100자입니다")
        private String title;

        @Size(max = 3000, message = "content는 최대 3000자입니다")
        private String content;

        private List<String> photoUrls;
        private String videoUrl;
        private Integer videoDurationSeconds;
        private String videoThumbnailUrl;

        private Set<@Size(max = 10, message = "해시태그는 10자 이하만 가능합니다") String> hashtags;

    }

    // -------------------
    // 응답 DTO
    // -------------------

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
    public static class UpdateResponse {
        private String message;
        private Long recordId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeleteResponse {
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
        private String title;
        private String content;
        private Long likeCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long venueId;
        private String venueName;
        private List<MediaItem> media;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class MediaItem {
            private Long id;
            private MediaType type;
            private String fileUrl;
            private String thumbnailUrl;     // PHOTO일 땐 null
            private Integer durationSeconds; // PHOTO일 땐 null
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordDetailResponse {
        private Long recordId;
        private String title;
        private String content;
        private Long likeCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // 작성자
        private Long writerId;
        private String writerNickname;
        private String writerProfileImgUrl;

        // 전시
        private Long exhibitionId;
        private String exhibitionTitle;

        // 장소(있으면)
        private Long venueId;
        private String venueName;

        // 미디어/해시태그
        private List<RecordMediaDto> mediaList;
        private Set<String> hashtags;
    }


    /**
     * 전시기록 목록 조회 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordListResponse {
        private Long recordId;
        private String title;
        private String content;
        private Long likeCount;
        private LocalDateTime createdAt;

        // 작성자 정보
        private String writerNickname;
        private String writerProfileImgUrl; // User 엔티티에 프로필 이미지 URL이 있다고 가정

        // 미디어 정보 (예: 첫 번째 이미지 썸네일)
        private List<RecordMediaDto> mediaList;

        // 해시태그 목록
        private Set<String> hashtags;

        private String exhibitionTitle;
    }

    /**
     * 전시기록 미디어 정보 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordMediaDto {
        private Long mediaId;
        private String mediaType; // "PHOTO" or "VIDEO"
        private String fileUrl;
        private String thumbnailUrl; // 썸네일 URL

        public RecordMediaDto(RecordMedia media) {
            this.mediaId = media.getId();
            this.mediaType = media.getMediaType().name();
            this.fileUrl = media.getFileUrl();
            this.thumbnailUrl = media.getThumbnailUrl();
        }
    }
}