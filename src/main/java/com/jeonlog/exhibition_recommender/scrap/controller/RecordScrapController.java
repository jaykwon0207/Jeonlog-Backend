package com.jeonlog.exhibition_recommender.scrap.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.scrap.domain.RecordScrap;
import com.jeonlog.exhibition_recommender.scrap.dto.RecordScrapListResponseDto;
import com.jeonlog.exhibition_recommender.scrap.dto.RecordScrapResponseDto;
import com.jeonlog.exhibition_recommender.scrap.service.RecordScrapService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class RecordScrapController {

    private final RecordScrapService recordScrapService;

    // 스크랩 토글 (추가 / 취소)
    @PutMapping("/{recordId}/scrap")
    public ApiResponse<RecordScrapResponseDto> toggleScrap(
            @PathVariable Long recordId,
            @CurrentUser User user
    ) {
        RecordScrapResponseDto response = recordScrapService.toggleScrapWithCount(recordId, user);
        return ApiResponse.ok(response);
    }

    //내가 스크랩한 글 전부 조회
    @GetMapping("/scraps")
    public ApiResponse<List<RecordScrapListResponseDto>> getMyScraps(@CurrentUser User user) {
        List<RecordScrap> scraps = recordScrapService.getUserScraps(user);

        List<RecordScrapListResponseDto> responseList = scraps.stream()
                .map(scrap -> {
                    var record = scrap.getRecord();
                    String thumbnail = record.getMediaList().isEmpty()
                            ? null
                            : record.getMediaList().get(0).getThumbnailUrl();

                    return RecordScrapListResponseDto.builder()
                            .recordId(record.getId())
                            .title(record.getExhibition().getTitle()) // 전시 제목
                            .contentPreview(record.getContent() != null && record.getContent().length() > 50
                                    ? record.getContent().substring(0, 50) + "..."
                                    : record.getContent())
                            .thumbnailUrl(thumbnail)
                            .scrappedAt(scrap.getScrappedAt())
                            .build();
                })
                .toList();

        return ApiResponse.ok(responseList);
    }



}
