// record/controller/ExhibitionRecordController.java
package com.jeonlog.exhibition_recommender.record.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.domain.RecordMedia;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto.CreateRequest;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.record.repository.RecordMediaRepository;
import com.jeonlog.exhibition_recommender.record.service.ExhibitionRecordService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.exception.UserNotFoundException;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ExhibitionRecordController {

    private final ExhibitionRecordService exhibitionRecordService;
    private final UserRepository userRepository;
    private final RecordMediaRepository mediaRepository;
    private final ExhibitionRecordRepository recordRepository;



    // 전시기록 생성
    @PostMapping("/exhibitions/{id}/records")
    public ApiResponse<ExhibitionRecordDto.CreateResponse> createRecord(
            @PathVariable("id") Long exhibitionId,
            @AuthenticationPrincipal String email,
            @RequestBody @Validated CreateRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long recordId = exhibitionRecordService.addRecord(exhibitionId, user, request);

        return ApiResponse.ok(
                ExhibitionRecordDto.CreateResponse.builder()
                        .recordId(recordId)
                        .message("전시기록이 전시기록 목록에 추가되었습니다.")
                        .build()
        );
    }

    // 전시기록 삭제
    @DeleteMapping("/exhibitions/{exhibitionId}/records/{recordId}")
    public ApiResponse<ExhibitionRecordDto.DeleteResponse> deleteRecord(
            @PathVariable("exhibitionId") Long exhibitionId,
            @PathVariable("recordId") Long recordId,
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        exhibitionRecordService.deleteRecord(exhibitionId, recordId, user);

        return ApiResponse.ok(
                ExhibitionRecordDto.DeleteResponse.builder()
                        .recordId(recordId)
                        .message("전시기록이 삭제되었습니다.")
                        .build()
        );
    }

    // 내가 작성한 전시기록 목록
    @GetMapping("/users/records")
    public ApiResponse<List<ExhibitionRecordDto.MyRecordSummary>> getMyRecords(
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return ApiResponse.ok(exhibitionRecordService.getMyRecords(user));
    }

    // 전시기록 수정
    @PutMapping("/exhibitions/{exhibitionId}/records/{recordId}")
    public ApiResponse<ExhibitionRecordDto.UpdateResponse> updateRecord(
            @PathVariable Long exhibitionId,
            @PathVariable Long recordId,
            @AuthenticationPrincipal String email,
            @RequestBody @Validated ExhibitionRecordDto.UpdateRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long updatedId = exhibitionRecordService.updateRecord(exhibitionId, recordId, user, request);

        return ApiResponse.ok(
                ExhibitionRecordDto.UpdateResponse.builder()
                        .recordId(updatedId)
                        .message("전시기록이 수정되었습니다.")
                        .build()
        );
    }

    // 전시기록에 미디어 추가
    @PostMapping("/exhibitions/{exhibitionId}/records/{recordId}/media")
    public ApiResponse<Long> addMedia(
            @PathVariable Long exhibitionId,
            @PathVariable Long recordId,
            @AuthenticationPrincipal String email,
            @RequestBody RecordMedia media
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        ExhibitionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시기록을 찾을 수 없습니다."));

        // 권한 체크 (내 기록인지 확인)
        if (!record.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 기록만 수정할 수 있습니다.");
        }

        RecordMedia saved = RecordMedia.builder()
                .mediaType(media.getMediaType())
                .fileUrl(media.getFileUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .durationSeconds(media.getDurationSeconds())
                .record(record)
                .build();

        mediaRepository.save(saved);

        return ApiResponse.ok(saved.getId());
    }
}