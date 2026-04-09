package com.jeonlog.exhibition_recommender.notification.service;

import com.jeonlog.exhibition_recommender.notification.domain.*;
import com.jeonlog.exhibition_recommender.notification.dto.NotificationListResponse;
import com.jeonlog.exhibition_recommender.notification.dto.NotificationResponse;
import com.jeonlog.exhibition_recommender.notification.repository.NotificationRepository;
import com.jeonlog.exhibition_recommender.notification.repository.PushTokenRepository;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PushTokenRepository pushTokenRepository;
    private final FcmPushClient fcmPushClient;
    private final UserRepository userRepository;

    @Transactional
    public void registerPushToken(Long userId, String token, String platform) {
        PushToken pt = pushTokenRepository.findByUserId(userId)
                .orElse(PushToken.builder()
                        .userId(userId)
                        .token(token)
                        .platform(platform == null ? "FCM" : platform)
                        .isActive(true)
                        .build());

        pt.updateToken(token, platform == null ? "FCM" : platform);
        pushTokenRepository.save(pt);
        log.info("[NOTI] push_token_registered userId={} platform={}", userId, pt.getPlatform());
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(Long receiverUserId, Long cursor, int size) {
        int pageSize = Math.min(Math.max(size, 1), 50);

        Slice<Notification> slice = (cursor == null)
                ? notificationRepository.findByReceiverUserIdOrderByIdDesc(receiverUserId, PageRequest.of(0, pageSize))
                : notificationRepository.findByReceiverUserIdAndIdLessThanOrderByIdDesc(receiverUserId, cursor, PageRequest.of(0, pageSize));

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

    //  알림 생성

    @Transactional
    public void notifyRecordLike(Long recordId, Long recordOwnerUserId, Long actorUserId, String actorNickname) {
        if (recordOwnerUserId.equals(actorUserId)) {
            log.debug("[NOTI] skipped_self_notification type=RECORD_LIKE userId={} targetId={}", actorUserId, recordId);
            return;
        }

        String nick = normalizeNick(actorNickname);
        String title = "좋아요";
        String message = "내 전시기록에 좋아요를 눌렀어요.";

        Notification saved = notificationRepository.save(Notification.builder()
                .receiverUserId(recordOwnerUserId)
                .actorUserId(actorUserId)
                .actorNickname(nick)
                .message(message)
                .type(NotificationType.RECORD_LIKE)
                .targetType(TargetType.EXHIBITION_RECORD)
                .targetId(recordId)
                .title(title)
                .body(message)
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
        log.info(
                "[NOTI] created type=RECORD_LIKE notificationId={} receiverUserId={} actorUserId={} targetId={}",
                saved.getId(),
                recordOwnerUserId,
                actorUserId,
                recordId
        );

        sendPushIfPossible(recordOwnerUserId, title, message, data);
    }

    @Transactional
    public void notifyRecordComment(Long recordId,
                                    Long recordOwnerUserId,
                                    Long actorUserId,
                                    String actorNickname,
                                    String commentPreview) {
        if (recordOwnerUserId.equals(actorUserId)) {
            log.debug("[NOTI] skipped_self_notification type=RECORD_COMMENT userId={} targetId={}", actorUserId, recordId);
            return;
        }

        String nick = normalizeNick(actorNickname);
        String preview = makePreview(commentPreview, 40);

        String title = "댓글";
        String message = (preview == null || preview.isBlank())
                ? "내 전시기록에 댓글을 달았어요."
                : "댓글을 달았어요: \"" + preview + "\"";

        Notification saved = notificationRepository.save(Notification.builder()
                .receiverUserId(recordOwnerUserId)
                .actorUserId(actorUserId)
                .actorNickname(nick)
                .message(message)
                .type(NotificationType.RECORD_COMMENT)
                .targetType(TargetType.EXHIBITION_RECORD)
                .targetId(recordId)
                .title(title)
                .body(message)
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
        log.info(
                "[NOTI] created type=RECORD_COMMENT notificationId={} receiverUserId={} actorUserId={} targetId={}",
                saved.getId(),
                recordOwnerUserId,
                actorUserId,
                recordId
        );

        sendPushIfPossible(recordOwnerUserId, title, message, data);
    }

    @Transactional
    public void notifyFollow(Long followedUserId, Long actorUserId, String actorNickname) {
        if (followedUserId.equals(actorUserId)) {
            log.debug("[NOTI] skipped_self_notification type=FOLLOW userId={} targetId={}", actorUserId, actorUserId);
            return;
        }

        String nick = normalizeNick(actorNickname);
        String title = "팔로우";
        String message = "회원님을 팔로우하기 시작했어요.";

        Notification saved = notificationRepository.save(Notification.builder()
                .receiverUserId(followedUserId)
                .actorUserId(actorUserId)
                .actorNickname(nick)
                .message(message)
                .type(NotificationType.FOLLOW)
                .targetType(TargetType.USER)
                .targetId(actorUserId)
                .title(title)
                .body(message)
                .dedupKey(null)
                .build());

        Map<String, Object> data = new HashMap<>();
        data.put("type", "FOLLOW");
        data.put("targetType", "USER");
        data.put("targetId", actorUserId);
        data.put("actorUserId", actorUserId);
        if (nick != null) data.put("actorNickname", nick);
        data.put("message", message);
        data.put("notificationId", saved.getId());
        log.info(
                "[NOTI] created type=FOLLOW notificationId={} receiverUserId={} actorUserId={} targetId={}",
                saved.getId(),
                followedUserId,
                actorUserId,
                actorUserId
        );

        sendPushIfPossible(followedUserId, title, message, data);
    }

    // 전시 종료 2주 전 알림
    @Transactional
    public void notifyExhibitionEndingSoon(
            Long receiverUserId,
            Long exhibitionId,
            String endDateIso,
            String exhibitionTitle
    ) {
        String dedupKey = "EXHIBITION_ENDING_SOON:" + receiverUserId + ":" + exhibitionId + ":" + endDateIso;
        if (notificationRepository.existsByDedupKey(dedupKey)) {
            log.debug(
                    "[NOTI] skipped_dedup type=EXHIBITION_ENDING_SOON receiverUserId={} targetId={} dedupKey={}",
                    receiverUserId,
                    exhibitionId,
                    dedupKey
            );
            return;
        }

        String title = "전시 종료 알림";
        String exTitle = (exhibitionTitle == null || exhibitionTitle.isBlank()) ? "찜한 전시" : exhibitionTitle;
        String message = "[" + exTitle + "] 전시가 2주 후 종료돼요. 놓치지 마세요!";

        Notification saved = notificationRepository.save(Notification.builder()
                .receiverUserId(receiverUserId)
                .actorUserId(null)
                .actorNickname(null)
                .message(message)
                .type(NotificationType.EXHIBITION_ENDING_SOON)
                .targetType(TargetType.EXHIBITION)
                .targetId(exhibitionId)
                .title(title)
                .body(message)
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
        log.info(
                "[NOTI] created type=EXHIBITION_ENDING_SOON notificationId={} receiverUserId={} targetId={}",
                saved.getId(),
                receiverUserId,
                exhibitionId
        );

        sendPushIfPossible(receiverUserId, title, message, data);
    }

    // 서비스 공지: 이미지 최대 5장, 썸네일 없음
    @Transactional
    public void notifyServiceAnnouncementToAll(
            Long announcementId,
            String title,
            String body,
            List<String> imageUrls,
            boolean pushEnabled
    ) {
        long startedAt = System.currentTimeMillis();
        List<Long> userIds = userRepository.findAllUserIds();
        log.info(
                "[NOTI] service_announcement_start announcementId={} receiverCount={} pushEnabled={}",
                announcementId,
                userIds.size(),
                pushEnabled
        );

        int chunkSize = 500;
        for (int i = 0; i < userIds.size(); i += chunkSize) {
            List<Long> chunk = userIds.subList(i, Math.min(i + chunkSize, userIds.size()));

            List<Notification> batch = chunk.stream()
                    .map(receiverId -> Notification.builder()
                            .receiverUserId(receiverId)
                            .actorUserId(null)
                            .actorNickname(null)
                            .message(body)
                            .type(NotificationType.SERVICE_ANNOUNCEMENT)
                            .targetType(TargetType.SERVICE_ANNOUNCEMENT)
                            .targetId(announcementId)
                            .title(title)
                            .body(body)
                            .dedupKey("SERVICE_ANNOUNCEMENT:" + announcementId + ":" + receiverId)
                            .build())
                    .toList();

            notificationRepository.saveAll(batch);
            log.info(
                    "[NOTI] service_announcement_chunk_saved announcementId={} chunkStart={} chunkSize={}",
                    announcementId,
                    i,
                    chunk.size()
            );
        }

        if (!pushEnabled) {
            log.info(
                    "[NOTI] service_announcement_completed announcementId={} durationMs={} pushSent=false",
                    announcementId,
                    System.currentTimeMillis() - startedAt
            );
            return;
        }

        List<String> urls = (imageUrls == null) ? List.of() : imageUrls;
        if (urls.size() > 5) urls = urls.subList(0, 5);

        List<String> finalUrls = urls;
        final int[] pushedCount = {0};
        pushTokenRepository.findAll().forEach(pt -> {
            if (!pt.isActive()) return;

            Map<String, Object> data = new HashMap<>();
            data.put("type", "SERVICE_ANNOUNCEMENT");
            data.put("targetType", "SERVICE_ANNOUNCEMENT");
            data.put("targetId", announcementId);
            if (!finalUrls.isEmpty()) data.put("imageUrls", String.join(",", finalUrls));

            fcmPushClient.send(pt.getToken(), title, body, data);
            pushedCount[0]++;
        });
        log.info(
                "[NOTI] service_announcement_completed announcementId={} durationMs={} pushSent=true pushTargetCount={}",
                announcementId,
                System.currentTimeMillis() - startedAt,
                pushedCount[0]
        );
    }

    // 내부 유틸

    private void sendPushIfPossible(Long receiverUserId, String title, String pushBody, Map<String, Object> data) {
        pushTokenRepository.findByUserId(receiverUserId).ifPresent(pt -> {
            if (!pt.isActive()) {
                log.debug("[NOTI] push_skipped reason=inactive_token receiverUserId={}", receiverUserId);
                return;
            }
            fcmPushClient.send(pt.getToken(), title, pushBody, data);
            log.debug("[NOTI] push_sent receiverUserId={} type={}", receiverUserId, data.get("type"));
        });
    }

    private String normalizeNick(String actorNickname) {
        if (actorNickname == null) return null;
        String s = actorNickname.trim();
        return s.isBlank() ? null : s;
    }

    private String makePreview(String raw, int maxLen) {
        if (raw == null) return null;
        String s = raw.trim().replaceAll("\\s+", " ");
        if (s.isBlank()) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
}
