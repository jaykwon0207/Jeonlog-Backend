package com.jeonlog.exhibition_recommender.scrap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RecordScrapResponseDto {
    private boolean scrapped;  // true면 스크랩됨, false면 취소됨
    private long scrapCount;   // 현재 해당 기록의 총 스크랩 수
    private String message;    // 상태 메시지 ("스크랩 성공", "스크랩 취소")
}
