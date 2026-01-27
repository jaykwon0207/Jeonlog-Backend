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
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;

import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PushTokenRepository pushTokenRepository;
    private final ExpoPushClient expoPushClient;
    private final UserRepository userRepository;


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

        List<Notification> notifications = slice.getContent();

        List<Long> actorIds = notifications.stream()
                .map(Notification::getActorUserId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, String> profileUrlMap = actorIds.isEmpty()
                ? Map.of()
                : userRepository.findProfileImageUrlsByIds(actorIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        UserRepository.UserProfileImageProjection::getId,
                        UserRepository.UserProfileImageProjection::getProfileImageUrl
                ));

        List<NotificationResponse> items = notifications.stream()
                .map(n -> NotificationResponse.from(
                        n,
                        n.getActorUserId() == null ? null : profileUrlMap.get(n.getActorUserId())
                ))
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

        String nick = normalizeNick(actorNickname); // null 가능
        String title = "좋아요";

        // message에는 "님이"를 넣지 않음
        String message = "내 전시기록에 좋아요를 눌렀어요.";
        String body = buildActorBody(nick, message); // "닉네임님이 message"

        Notification saved = notificationRepository.save(Notification.builder()
                .receiverUserId(recordOwnerUserId)
                .actorUserId(actorUserId)
                .actorNickname(nick)
                .message(message)
                .type(NotificationType.RECORD_LIKE)
                .targetType(TargetType.EXHIBITION_RECORD)
                .targetId(recordId)
                .title(title)
                .body(body)
                .dedupKey(null)
                .build());

        Map<String, Object> data = new HashMap<>();
        data.put("type", "RECORD_LIKE");
        data.put("targetType", "EXHIBITION_RECORD");
        data.put("targetId", recordId);
        data.put("actorUserId", actorUserId);
        if (nick != null) data.put("actorNickname", nick);
        data.put("message", message);
        data.put("notificationId", saved.getId());

        sendPushIfPossible(recordOwnerUserId, title, body, data);
    }

    @Transactional
    public void notifyRecordComment(Long recordId,
                                    Long recordOwnerUserId,
                                    Long actorUserId,
                                    String actorNickname,
                                    String commentPreview) {
        if (recordOwnerUserId.equals(actorUserId)) return;

        String nick = normalizeNick(actorNickname);
        String preview = makePreview(commentPreview, 40);

        String title = "댓글";

        String message = (preview == null || preview.isBlank())
                ? "내 전시기록에 댓글을 달았어요."
                : "댓글을 달았어요: \"" + preview + "\"";

        String body = buildActorBody(nick, message);

        Notification saved = notificationRepository.save(Notification.builder()
                .receiverUserId(recordOwnerUserId)
                .actorUserId(actorUserId)
                .actorNickname(nick)
                .message(message)
                .type(NotificationType.RECORD_COMMENT)
                .targetType(TargetType.EXHIBITION_RECORD)
                .targetId(recordId)
                .title(title)
                .body(body)
                .dedupKey(null)
                .build());

        Map<String, Object> data = new HashMap<>();
        data.put("type", "RECORD_COMMENT");
        data.put("targetType", "EXHIBITION_RECORD");
        data.put("targetId", recordId);
        data.put("actorUserId", actorUserId);
        if (nick != null) data.put("actorNickname", nick);
        data.put("message", message);
        if (preview != null) data.put("commentPreview", preview);
        data.put("notificationId", saved.getId());

        sendPushIfPossible(recordOwnerUserId, title, body, data);
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

         // 시스템 알림은 actorNickname 없음. message 그대로 쓰면 됨
         String message = "[" + exTitle + "] 전시가 2주 후 종료돼요. 놓치지 마세요!";
         String body = message;

         Notification saved = notificationRepository.save(Notification.builder()
                 .receiverUserId(receiverUserId)
                 .actorUserId(null)
                 .actorNickname(null)
                 .message(message)
                 .type(NotificationType.EXHIBITION_ENDING_SOON)
                 .targetType(TargetType.EXHIBITION)
                 .targetId(exhibitionId)
                 .title(title)
                 .body(body)
                 .dedupKey(dedupKey)
                 .build());

         Map<String, Object> data = new HashMap<>();
         data.put("type", "EXHIBITION_ENDING_SOON");
         data.put("targetType", "EXHIBITION");
         data.put("targetId", exhibitionId);
         data.put("exhibitionTitle", exTitle);
         data.put("endDate", endDateIso);
         data.put("message", message);
         data.put("notificationId", saved.getId());

         sendPushIfPossible(receiverUserId, title, body, data);
     }

    private void sendPushIfPossible(Long receiverUserId, String title, String body, Map<String, Object> data) {
        pushTokenRepository.findByUserId(receiverUserId).ifPresent(pt -> {
            if (!pt.isActive()) return;
            expoPushClient.send(pt.getToken(), title, body, data);
        });
    }

    private String normalizeNick(String actorNickname) {
        if (actorNickname == null) return null;
        String s = actorNickname.trim();
        return s.isBlank() ? null : s;
    }

    // actor 기반 알림 body: OS/하위호환용 (프론트는 actorNickname + "\n" + message로 조립)
    private String buildActorBody(String nick, String message) {
        String who = (nick == null) ? "누군가" : nick;
        return who + "님이 " + message;
    }

    private String makePreview(String raw, int maxLen) {
        if (raw == null) return null;
        String s = raw.trim().replaceAll("\\s+", " ");
        if (s.isBlank()) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
}