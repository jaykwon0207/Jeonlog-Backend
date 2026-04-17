package com.jeonlog.exhibition_recommender.notification.service;

import com.jeonlog.exhibition_recommender.notification.domain.ServiceAnnouncement;
import com.jeonlog.exhibition_recommender.notification.dto.ServiceAnnouncementCreateRequest;
import com.jeonlog.exhibition_recommender.notification.repository.ServiceAnnouncementRepository;
import com.jeonlog.exhibition_recommender.user.domain.Role;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceAnnouncementService {

    private final ServiceAnnouncementRepository serviceAnnouncementRepository;
    private final NotificationService notificationService;

    @Transactional
    public Long createAndBroadcast(User user, ServiceAnnouncementCreateRequest req) {
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }

        ServiceAnnouncement saved = serviceAnnouncementRepository.save(
                ServiceAnnouncement.builder()
                        .title(req.getTitle())
                        .body(req.getBody())
                        .pushEnabled(req.isPushEnabled())
                        .build()
        );

        List<String> imageUrls = (req.getImageUrls() == null) ? List.of() : req.getImageUrls();
        if (imageUrls.size() > 5) imageUrls = imageUrls.subList(0, 5);

        notificationService.notifyServiceAnnouncementToAll(
                saved.getId(),
                saved.getTitle(),
                saved.getBody(),
                imageUrls,
                saved.isPushEnabled()
        );

        return saved.getId();
    }
}
