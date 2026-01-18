package com.jeonlog.exhibition_recommender.notification.dto;

import com.jeonlog.exhibition_recommender.notification.domain.Notification;
import com.jeonlog.exhibition_recommender.notification.domain.NotificationType;
import com.jeonlog.exhibition_recommender.notification.domain.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private TargetType targetType;
    private Long targetId;
    private String title;
    private String body;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .targetType(n.getTargetType())
                .targetId(n.getTargetId())
                .title(n.getTitle())
                .body(n.getBody())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
