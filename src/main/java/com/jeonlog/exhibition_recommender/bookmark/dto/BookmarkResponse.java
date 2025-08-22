package com.jeonlog.exhibition_recommender.bookmark.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BookmarkResponse {
    private final Long exhibitionId;
    private final String exhibitionTitle;
    private final boolean bookmarked;     // true=추가, false=삭제
    private final boolean notifyEnabled;  // 현재 알림 상태
    private final long bookmarkCount;     // 전시 총 북마크 수

    @Builder
    public BookmarkResponse(Long exhibitionId, String exhibitionTitle,
                            boolean bookmarked, boolean notifyEnabled, long bookmarkCount) {
        this.exhibitionId = exhibitionId;
        this.exhibitionTitle = exhibitionTitle;
        this.bookmarked = bookmarked;
        this.notifyEnabled = notifyEnabled;
        this.bookmarkCount = bookmarkCount;
    }

    public static BookmarkResponse of(Exhibition ex, boolean bookmarked,
                                      boolean notifyEnabled, long count) {
        return BookmarkResponse.builder()
                .exhibitionId(ex.getId())
                .exhibitionTitle(ex.getTitle())
                .bookmarked(bookmarked)
                .notifyEnabled(notifyEnabled)
                .bookmarkCount(count)
                .build();
    }
}