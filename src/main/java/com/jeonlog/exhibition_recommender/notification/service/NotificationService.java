package com.jeonlog.exhibition_recommender.notification.service;

import com.jeonlog.exhibition_recommender.notification.domain.*;
import com.jeonlog.exhibition_recommender.notification.dto.NotificationListResponse;
import com.jeonlog.exhibition_recommender.notification.dto.NotificationResponse;
import com.jeonlog.exhibition_recommender.notification.repository.NotificationRepository;
import com.jeonlog.exhibition_recommender.notification.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PushTokenRepository pushTokenRepository;
    private final ExpoPushClient expoPushClient;

    @Transactional
    public void registerPushToken(Long userId, String token, String platform) {
        PushToken pt = pushTokenRepository.findByUserId(userId)
                .orElse(PushToken.builder()
                        .userId(userId)
                        .token(token)
                        .platform(platform == null ? "EXPO" : platform)
                        .isActive(true)
                        .build());

        pt.updateToken(token, platform == null ? "EXPO" : platform);
        pushTokenRepository.save(pt);
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(Long receiverUserId, Long cursor, int size) {
        int pageSize = Math.min(Math.max(size, 1), 50);
        Slice<Notification> slice;

        if (cursor == null) {
            slice = notificationRepository.findByReceiverUserIdOrderByIdDesc(receiverUserId, PageRequest.of(0, pageSize));
        } else {
            slice = notificationRepository.findByReceiverUserIdAndIdLessThanOrderByIdDesc(receiverUserId, cursor, PageRequest.of(0, pageSize));
        }

        List<NotificationResponse> items = slice.getContent().stream()
                .map(NotificationResponse::from)
                .toList();

        Long nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).getId();

        return NotificationListResponse.builder()
                .items(items)
                .nextCursor(slice.hasNext() ? nextCursor : null)
                .hasNext(slice.hasNext())
                .build();
    }

    @Transactional
    public void markAsRead(Long receiverUserId, Long notificationId) {
        Notification n = notificationRepository.findByIdAndReceiverUserId(notificationId, receiverUserId)
                .orElseThrow(() -> new IllegalArgumentException("notification not found"));
        n.markAsRead();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long receiverUserId) {
        return notificationRepository.countByReceiverUserIdAndIsReadFalse(receiverUserId);
    }


    // 기록 좋아요 알림 (닉네임 포함)
    @Transactional
    public void notifyRecordLike(Long recordId, Long recordOwnerUserId, Long actorUserId, String actorNickname) {
        if (recordOwnerUserId.equals(actorUserId)) return;

        String safeNick = (actorNickname == null || actorNickname.isBlank()) ? "누군가" : actorNickname;

        String title = "좋아요";
        String body = safeNick + "님이 내 전시기록에 좋아요를 눌렀어요.";

        Notification saved = notificationRepository.save(Notification.builder()
                .receiverUserId(recordOwnerUserId)
                .actorUserId(actorUserId)
                .type(NotificationType.RECORD_LIKE)
                .targetType(TargetType.EXHIBITION_RECORD)
                .targetId(recordId)
                .title(title)
                .body(body)
                .dedupKey(null)
                .build());

        sendPushIfPossible(recordOwnerUserId, title, body, Map.of(
                "type", "RECORD_LIKE",
                "targetType", "EXHIBITION_RECORD",
                "targetId", recordId,
                "actorUserId", actorUserId,
                "actorNickname", safeNick,
                "notificationId", saved.getId()
        ));
    }


    // 기록 댓글 알림 (닉네임 + 댓글 미리보기 포함)
    @Transactional
    public void notifyRecordComment(Long recordId,
                                    Long recordOwnerUserId,
                                    Long actorUserId,
                                    String actorNickname,
                                    String commentPreview) {
        if (recordOwnerUserId.equals(actorUserId)) return;

        String safeNick = (actorNickname == null || actorNickname.isBlank()) ? "누군가" : actorNickname;
        String preview = makePreview(commentPreview, 40);

        String title = "댓글";
        String body = (preview == null || preview.isBlank())
                ? safeNick + "님이 내 전시기록에 댓글을 달았어요."
                : safeNick + "님이 댓글을 달았어요: \"" + preview + "\"";

        Notification saved = notificationRepository.save(Notification.builder()
                .receiverUserId(recordOwnerUserId)
                .actorUserId(actorUserId)
                .type(NotificationType.RECORD_COMMENT)
                .targetType(TargetType.EXHIBITION_RECORD)
                .targetId(recordId)
                .title(title)
                .body(body)
                .dedupKey(null)
                .build());

        sendPushIfPossible(recordOwnerUserId, title, body, Map.of(
                "type", "RECORD_COMMENT",
                "targetType", "EXHIBITION_RECORD",
                "targetId", recordId,
                "actorUserId", actorUserId,
                "actorNickname", safeNick,
                "commentPreview", preview,
                "notificationId", saved.getId()
        ));
    }


     // 전시 종료 2주 전 알림 (전시명 포함, dedupKey로 중복 방지)
    @Transactional
    public void notifyExhibitionEndingSoon(Long receiverUserId,
                                           Long exhibitionId,
                                           String endDateIso,
                                           String exhibitionTitle) {
        String dedupKey = "EXHIBITION_ENDING_SOON:" + receiverUserId + ":" + exhibitionId + ":" + endDateIso;
        if (notificationRepository.existsByDedupKey(dedupKey)) return;

        String title = "전시 종료 알림";
        String exTitle = (exhibitionTitle == null || exhibitionTitle.isBlank()) ? "찜한 전시" : exhibitionTitle;
        String body = "[" + exTitle + "] 전시가 2주 후 종료돼요. 놓치지 마세요!";

        Notification saved = notificationRepository.save(Notification.builder()
                .receiverUserId(receiverUserId)
                .actorUserId(null)
                .type(NotificationType.EXHIBITION_ENDING_SOON)
                .targetType(TargetType.EXHIBITION)
                .targetId(exhibitionId)
                .title(title)
                .body(body)
                .dedupKey(dedupKey)
                .build());

        sendPushIfPossible(receiverUserId, title, body, Map.of(
                "type", "EXHIBITION_ENDING_SOON",
                "targetType", "EXHIBITION",
                "targetId", exhibitionId,
                "exhibitionTitle", exTitle,
                "endDate", endDateIso,
                "notificationId", saved.getId()
        ));
    }

    private void sendPushIfPossible(Long receiverUserId, String title, String body, Map<String, Object> data) {
        pushTokenRepository.findByUserId(receiverUserId).ifPresent(pt -> {
            if (!pt.isActive()) return;
            expoPushClient.send(pt.getToken(), title, body, data);
        });
    }

    private String makePreview(String raw, int maxLen) {
        if (raw == null) return null;
        String s = raw.trim().replaceAll("\\s+", " ");
        if (s.isBlank()) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
}
