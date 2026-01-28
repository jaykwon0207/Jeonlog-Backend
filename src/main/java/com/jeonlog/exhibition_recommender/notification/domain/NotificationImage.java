package com.jeonlog.exhibition_recommender.notification.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_images",
        indexes = {
                @Index(name = "idx_notification_images_notification_id", columnList = "notification_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private int sortOrder; // 0~4 권장
}
