package com.jeonlog.exhibition_recommender.comment.dto;

import com.jeonlog.exhibition_recommender.comment.domain.RecordComment;
import com.jeonlog.exhibition_recommender.user.domain.User;
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
    private LocalDateTime createdAt;

    // 작성자 정보 (닉네임 기준)
    private Long writerId;
    private String writerNickname;
    private String writerProfileImgUrl;

    // 대댓글 포함
    private List<RecordCommentResponse> replies;

    public static RecordCommentResponse from(RecordComment comment) {
        User u = comment.getUser();

        return RecordCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())

                .writerId(u.getId())
                .writerNickname(u.getNickname())
                .writerProfileImgUrl(u.getProfileImageUrl())

                .replies(comment.getReplies().stream()
                        .map(RecordCommentResponse::from)
                        .toList())
                .build();
    }
}
