package com.jeonlog.exhibition_recommender.comment.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.comment.dto.RecordCommentRequest;
import com.jeonlog.exhibition_recommender.comment.dto.RecordCommentResponse;
import com.jeonlog.exhibition_recommender.comment.service.RecordCommentService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records/{recordId}/comments")
@RequiredArgsConstructor
public class RecordCommentController {

    private final RecordCommentService commentService;

   //댓글 또는 대댓글 등록
    @PostMapping
    public ApiResponse<RecordCommentResponse> createComment(
            @PathVariable Long recordId,
            @CurrentUser User user,
            @RequestBody RecordCommentRequest request
    ) {
        RecordCommentResponse response = commentService.create(recordId, user.getId(), request);
        return ApiResponse.ok(response);
    }

    //특정 전시기록의 모든 댓글 조회
    @GetMapping
    public ApiResponse<List<RecordCommentResponse>> getComments(@PathVariable Long recordId) {
        return ApiResponse.ok(commentService.getComments(recordId));
    }

   //댓글 수정
    @PutMapping("/{commentId}")
    public ApiResponse<String> updateComment(
            @PathVariable Long recordId,
            @PathVariable Long commentId,
            @CurrentUser User user,
            @RequestBody RecordCommentRequest request
    ) {
        commentService.update(commentId, user.getId(), request);
        return ApiResponse.ok("댓글이 수정되었습니다.");
    }

    //댓글 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<String> deleteComment(
            @PathVariable Long recordId,
            @PathVariable Long commentId,
            @CurrentUser User user
    ) {
        commentService.delete(commentId, user.getId());
        return ApiResponse.ok("댓글이 삭제되었습니다.");
    }
}
