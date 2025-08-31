package com.jeonlog.exhibition_recommender.exhibition.controller;

import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionClickLogDto;
import com.jeonlog.exhibition_recommender.exhibition.service.ExhibitionClickLogService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exhibitions")
public class ExhibitionClickLogController {

    private final ExhibitionClickLogService clickLogService;
    private final UserRepository userRepository;

    //전시 클릭 로그 저장
    @PostMapping("/{id}/click")
    public ResponseEntity<?> saveClickLog(
            @PathVariable("id") Long exhibitionId,
            @Valid @RequestBody(required = false) ExhibitionClickLogDto requestDto
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        // JwtAuthenticationFilter에서 principal에 이메일을 넣어두었으므로 String 캐스팅
        String email = (String) authentication.getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        clickLogService.saveClick(exhibitionId, user, requestDto);

        return ResponseEntity.ok(Map.of("message", "클릭 로그가 저장되었습니다."));
    }

    // 특정 전시회의 연령대별 클릭 통계 조회
    @GetMapping("/{id}/click-stats/age-group")
    public ResponseEntity<?> getClickStatsByAgeGroupForExhibition(@PathVariable("id") Long exhibitionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            Map<String, Long> stats = clickLogService.getClickStatsByAgeGroupForExhibition(exhibitionId);
            return ResponseEntity.ok(stats);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "통계 조회 중 오류가 발생했습니다."));
        }
    }




    // 특정 전시회의 성별별 클릭 통계 조회
    @GetMapping("/{id}/click-stats/gender")
    public ResponseEntity<?> getClickStatsByGenderForExhibition(@PathVariable("id") Long exhibitionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            Map<String, Long> stats = clickLogService.getClickStatsByGenderForExhibition(exhibitionId);
            return ResponseEntity.ok(stats);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "통계 조회 중 오류가 발생했습니다."));
        }
    }
}
