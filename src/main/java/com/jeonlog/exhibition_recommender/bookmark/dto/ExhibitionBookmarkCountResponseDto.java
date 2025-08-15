package com.jeonlog.exhibition_recommender.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExhibitionBookmarkCountResponseDto {

    private Long exhibitionId;
    private Long bookmarkCount;
}
