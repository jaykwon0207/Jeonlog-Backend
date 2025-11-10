package com.jeonlog.exhibition_recommender.record.dto;

import lombok.Data;

@Data
public class RecordSearchCondition {
    private String exhibitionTitle; // 전시 제목 키워드
    private String hashtag;         // 해시태그 이름
}