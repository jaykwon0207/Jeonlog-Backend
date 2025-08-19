package com.jeonlog.exhibition_recommender.like.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LikeResponse {
    private final Long exhibitionId;
    private final String exhibitionTitle;
    private final boolean liked;   // true = 좋아요 추가, false = 취소
    private final long likeCount;  // 현재 좋아요 총 개수

    @Builder
    public LikeResponse(Long exhibitionId, String exhibitionTitle, boolean liked, long likeCount) {
        this.exhibitionId = exhibitionId;
        this.exhibitionTitle = exhibitionTitle;
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public static LikeResponse of(Exhibition exhibition, boolean liked, long likeCount) {
        return LikeResponse.builder()
                .exhibitionId(exhibition.getId())
                .exhibitionTitle(exhibition.getTitle())
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }
}