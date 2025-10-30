package com.jeonlog.exhibition_recommender.record.controller;

import com.jeonlog.exhibition_recommender.auth.model.CustomUserDetails;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.domain.RecordMedia;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto;
import com.jeonlog.exhibition_recommender.record.dto.ExhibitionRecordDto.CreateRequest;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.record.repository.RecordMediaRepository;
import com.jeonlog.exhibition_recommender.record.service.ExhibitionRecordService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ExhibitionRecordController {

    private final ExhibitionRecordService exhibitionRecordService;
    private final RecordMediaRepository mediaRepository;
    private final ExhibitionRecordRepository recordRepository;

    // 전시기록 생성
    @PostMapping("/exhibitions/{id}/records")
    public ApiResponse<ExhibitionRecordDto.CreateResponse> createRecord(
            @PathVariable("id") Long exhibitionId,
            @AuthenticationPrincipal(expression = "user") User user, // ✅ 변경
            @RequestBody @Validated CreateRequest request
    ) {
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
            @AuthenticationPrincipal(expression = "user") User user // ✅ 변경
    ) {
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
            @AuthenticationPrincipal(expression = "user") User user // ✅ 변경
    ) {
        return ApiResponse.ok(exhibitionRecordService.getMyRecords(user));
    }

    // 전시기록 수정
    @PutMapping("/exhibitions/{exhibitionId}/records/{recordId}")
    public ApiResponse<ExhibitionRecordDto.UpdateResponse> updateRecord(
            @PathVariable Long exhibitionId,
            @PathVariable Long recordId,
            @AuthenticationPrincipal(expression = "user") User user, // ✅ 변경
            @RequestBody @Validated ExhibitionRecordDto.UpdateRequest request
    ) {
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
            @AuthenticationPrincipal(expression = "user") User user, // ✅ 변경
            @RequestBody RecordMedia media
    ) {
        ExhibitionRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시기록을 찾을 수 없습니다."));

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