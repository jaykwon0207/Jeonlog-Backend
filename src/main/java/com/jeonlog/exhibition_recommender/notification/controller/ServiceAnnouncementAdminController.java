package com.jeonlog.exhibition_recommender.notification.controller;

import com.jeonlog.exhibition_recommender.notification.dto.ServiceAnnouncementCreateRequest;
import com.jeonlog.exhibition_recommender.notification.service.ServiceAnnouncementService;
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
    public ResponseEntity<?> create(@RequestBody ServiceAnnouncementCreateRequest req) {
        Long id = serviceAnnouncementService.createAndBroadcast(req);
        return ResponseEntity.ok(Map.of(
                "message", "공지 발행 완료",
                "announcementId", id
        ));
    }
}
