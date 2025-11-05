package com.jeonlog.exhibition_recommender.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecordCommentRequest {
    private String content;
    private Long parentId;
}
