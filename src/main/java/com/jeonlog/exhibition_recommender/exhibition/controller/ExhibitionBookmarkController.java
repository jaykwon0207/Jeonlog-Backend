package com.jeonlog.exhibition_recommender.exhibition.controller;

import com.jeonlog.exhibition_recommender.bookmark.dto.ExhibitionBookmarkCountResponseDto;
import com.jeonlog.exhibition_recommender.bookmark.service.ExhibitionBookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exhibitions")
public class ExhibitionBookmarkController {

    private final ExhibitionBookmarkService exhibitionBookmarkService;

    @GetMapping("/{exhibitionId}/bookmarks/count")
    public ResponseEntity<ExhibitionBookmarkCountResponseDto> getBookmarkCount(@PathVariable Long exhibitionId) {
        Long count = exhibitionBookmarkService.countBookmarksByExhibitionId(exhibitionId);
        return ResponseEntity.ok(new ExhibitionBookmarkCountResponseDto(exhibitionId, count));
    }

}
