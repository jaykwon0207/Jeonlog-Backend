package com.jeonlog.exhibition_recommender.bookmark.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookmarkRequest {

    // 북마크 추가 또는 알림 상태 변경 시 사용할 필드
    private boolean notifyEnabled;
}