package com.jeonlog.exhibition_recommender.notification.repository;

import com.jeonlog.exhibition_recommender.notification.domain.NotificationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationImageRepository extends JpaRepository<NotificationImage, Long> {

    interface ImageRow {
        Long getNotificationId();
        String getImageUrl();
        int getSortOrder();
    }

    @Query("""
        select ni.notificationId as notificationId, ni.imageUrl as imageUrl, ni.sortOrder as sortOrder
        from NotificationImage ni
        where ni.notificationId in :notificationIds
        order by ni.notificationId desc, ni.sortOrder asc
    """)
    List<ImageRow> findImagesByNotificationIds(@Param("notificationIds") List<Long> notificationIds);

    void deleteByNotificationId(Long notificationId);
}
