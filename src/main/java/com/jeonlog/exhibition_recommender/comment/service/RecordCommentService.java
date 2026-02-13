package com.jeonlog.exhibition_recommender.comment.service;


import com.jeonlog.exhibition_recommender.comment.domain.RecordComment;
import com.jeonlog.exhibition_recommender.comment.dto.RecordCommentRequest;
import com.jeonlog.exhibition_recommender.comment.dto.RecordCommentResponse;
import com.jeonlog.exhibition_recommender.comment.repository.RecordCommentRepository;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordCommentService {

    private final RecordCommentRepository commentRepository;
    private final ExhibitionRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    //댓글 또는 대댓글 생성
    @Transactional
    public RecordCommentResponse create(Long recordId, Long userId, RecordCommentRequest request) {
        ExhibitionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("전시기록을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RecordComment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
        }

        RecordComment comment = RecordComment.builder()
                .record(record)
                .user(user)
                .content(request.getContent())
                .parent(parentComment)
                .build();
        // 저장
        RecordComment saved = commentRepository.save(comment);

        //알림: record 작성자에게 (본인 댓글이면 NotificationService에서 return)
        String preview = saved.getContent();
        notificationService.notifyRecordComment(
                record.getId(),
                record.getUser().getId(),
                user.getId(),
                user.getNickname(),
                preview
        );

        return RecordCommentResponse.from(commentRepository.save(comment));
    }

     //특정 전시기록의 모든 댓글 조회
     //(부모 댓글만 가져오고, 각 댓글에 연결된 replies는 DTO 변환 시 포함)
    public List<RecordCommentResponse> getComments(Long recordId) {
        return commentRepository.findByRecordIdAndParentIsNullOrderByCreatedAtAsc(recordId)
                .stream()
                .map(RecordCommentResponse::from)
                .toList();
    }

    //댓글 수정
    @Transactional
    public void update(Long commentId, Long userId, RecordCommentRequest request) {
        RecordComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인의 댓글만 수정할 수 있습니다.");
        }

        comment.updateContent(request.getContent());
    }

   //댓글 삭제
    @Transactional
    public void delete(Long commentId, Long userId) {
        RecordComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인의 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }
}
