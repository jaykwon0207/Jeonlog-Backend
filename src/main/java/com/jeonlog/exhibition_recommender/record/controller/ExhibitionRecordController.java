package com.jeonlog.exhibition_recommender.record.controller;

import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto.CreateRequest;
import com.jeonlog.exhibition_recommender.record.service.ExhibitionRecordService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ExhibitionRecordController {

    private final ExhibitionRecordService exhibitionRecordService;
    private final UserRepository userRepository;

    @PostMapping("/exhibitions/{id}/records")
    public ResponseEntity<Map<String, Object>> createRecord(
            @PathVariable("id") Long exhibitionId,
            @AuthenticationPrincipal String email,
            @RequestBody @Validated CreateRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long recordId = exhibitionRecordService.addRecord(exhibitionId, user, request);

        return ResponseEntity.ok(
                Map.of(
                        "message", "전시기록이 전시기록 목록에 추가되었습니다.",
                        "recordId", recordId
                )
        );
    }

    @DeleteMapping("/exhibitions/{exhibitionId}/records/{recordId}")
    public ResponseEntity<Map<String, String>> deleteRecord(
            @PathVariable("exhibitionId") Long exhibitionId,
            @PathVariable("recordId") Long recordId,
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        exhibitionRecordService.deleteRecord(exhibitionId, recordId, user);

        return ResponseEntity.ok(Map.of("message", "전시기록이 삭제되었습니다."));
    }


    @GetMapping("/users/records")
    public ResponseEntity<List<ExhibitionRecordDto.MyRecordSummary>> getMyRecords(
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<ExhibitionRecordDto.MyRecordSummary> response = exhibitionRecordService.getMyRecords(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/exhibitions/{exhibitionId}/records/{recordId}")
    public ResponseEntity<Map<String, Object>> updateRecord(
            @PathVariable Long exhibitionId,
            @PathVariable Long recordId,
            @AuthenticationPrincipal String email,
            @RequestBody @Validated ExhibitionRecordDto.UpdateRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long updatedId = exhibitionRecordService.updateRecord(exhibitionId, recordId, user, request);

        return ResponseEntity.ok(
                Map.of(
                        "message", "전시기록이 수정되었습니다.",
                        "recordId", updatedId
                )
        );
    }
}
