package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.VisitRequest;
import com.jeonlog.exhibition_recommender.user.dto.VisitedExhibitionDto;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserVisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserVisitController {

    private final UserVisitService userVisitService;
    private final UserRepository userRepository;

    @PostMapping("/exhibitions/{id}/visit")
    public ResponseEntity<?> recordVisit(
            @PathVariable Long id,
            @RequestBody(required = false) VisitRequest request,
            @AuthenticationPrincipal String email) {

        User user = userRepository.findByEmail(email)
                        .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        userVisitService.recordVisit(id, email, request);
        return ResponseEntity.ok(Map.of("message", "전시 방문 기록이 저장되었습니다."));
    }

    @GetMapping("/users/visits")
    public ResponseEntity<List<VisitedExhibitionDto>> getVisitedExhibitions(@AuthenticationPrincipal String email) {
        List<VisitedExhibitionDto> visited = userVisitService.getVisitedExhibitions(email);

        return ResponseEntity.ok(visited);
    }
}