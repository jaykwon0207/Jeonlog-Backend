package com.jeonlog.exhibition_recommender.notification.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.notification.dto.ServiceAnnouncementCreateRequest;
import com.jeonlog.exhibition_recommender.notification.service.ServiceAnnouncementService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/announcements")
public class ServiceAnnouncementAdminController {

    private final ServiceAnnouncementService serviceAnnouncementService;

    @PostMapping
    public ResponseEntity<?> create(@CurrentUser User user, @RequestBody ServiceAnnouncementCreateRequest req) {
        Long id = serviceAnnouncementService.createAndBroadcast(user, req);
        return ResponseEntity.ok(Map.of(
                "message", "공지 발행 완료",
                "announcementId", id
        ));
    }
}
