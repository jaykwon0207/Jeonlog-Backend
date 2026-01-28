package com.jeonlog.exhibition_recommender.notification.dto;

import com.jeonlog.exhibition_recommender.notification.domain.Notification;
import com.jeonlog.exhibition_recommender.notification.domain.NotificationType;
import com.jeonlog.exhibition_recommender.notification.domain.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private String actorNickname;
    private String message;
    private String actorProfileImageUrl;

    private List<String> imageUrls;

    private NotificationType type;
    private TargetType targetType;
    private Long targetId;
    private String title;
    private String body;
    private boolean isRead;
    private LocalDateTime createdAt;


    public static NotificationResponse from(Notification n, String actorProfileImageUrl, List<String> imageUrls) {
        return NotificationResponse.builder()
                .id(n.getId())
                .actorNickname(n.getActorNickname())
                .message(n.getMessage())
                .actorProfileImageUrl(actorProfileImageUrl)
                .imageUrls(imageUrls)
                .type(n.getType())
                .targetType(n.getTargetType())
                .targetId(n.getTargetId())
                .title(n.getTitle())
                .body(n.getBody())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }


    public static NotificationResponse from(Notification n, String actorProfileImageUrl) {
        return from(n, actorProfileImageUrl, List.of());
    }


    public static NotificationResponse from(Notification n) {
        return from(n, null, List.of());
    }
}
