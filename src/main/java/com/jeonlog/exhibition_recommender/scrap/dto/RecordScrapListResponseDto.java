package com.jeonlog.exhibition_recommender.scrap.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecordScrapListResponseDto {
    private Long recordId;          // 전시기록 ID
    private String title;           // 전시기록 제목
    private String contentPreview;  // 전시기록 내용 일부 (미리보기)
    private String thumbnailUrl;    // 썸네일 URL
    private LocalDateTime scrappedAt; // 스크랩한 날짜
}
