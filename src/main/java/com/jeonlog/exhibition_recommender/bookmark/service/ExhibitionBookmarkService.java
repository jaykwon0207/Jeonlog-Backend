package com.jeonlog.exhibition_recommender.bookmark.service;

import com.jeonlog.exhibition_recommender.bookmark.repository.ExhibitionBookmarkRepository;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExhibitionBookmarkService {

    private final ExhibitionBookmarkRepository bookmarkRepository;
    private final ExhibitionRepository exhibitionRepository;

    public Long countBookmarksByExhibitionId(Long exhibitionId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시회가 존재하지 않습니다."));
        return bookmarkRepository.countByExhibition(exhibition);
    }
}
