package com.jeonlog.exhibition_recommender.notification.repository;

import com.jeonlog.exhibition_recommender.notification.domain.ServiceAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceAnnouncementRepository extends JpaRepository<ServiceAnnouncement, Long> {
}
