package com.jeonlog.exhibition_recommender.comment.dto;

import com.jeonlog.exhibition_recommender.comment.domain.RecordComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RecordCommentResponse {
    private Long id;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
    private List<RecordCommentResponse> replies; // 대댓글 포함

    public static RecordCommentResponse from(RecordComment comment) {
        return RecordCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorName(comment.getUser().getName())
                .createdAt(comment.getCreatedAt())
                .replies(comment.getReplies().stream()
                        .map(RecordCommentResponse::from)
                        .toList())
                .build();
    }
}

