package com.jeonlog.exhibition_recommender.recommendation.service;

import com.jeonlog.exhibition_recommender.recommendation.domain.InitialExhibition;
import com.jeonlog.exhibition_recommender.recommendation.repository.InitialExhibitionRepository;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.dto.InitialChoiceRequest;
import com.jeonlog.exhibition_recommender.recommendation.dto.InitialExhibitionDto;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InitialRecommendationService {

    private final InitialExhibitionRepository initialExhibitionRepository;
    private final UserGenreRepository userGenreRepository;

    // 초기 노출 리스트 (20개)
    @Transactional(readOnly = true)
    public List<InitialExhibitionDto> listInitialExhibitions() {
        return initialExhibitionRepository.findTop20ByOrderByIdAsc()
                .stream()
                .map(InitialExhibitionDto::from)
                .toList();
    }

    // 사용자 선택을 기반으로 장르/테마 가중치 반영 (+0.03)
    @Transactional
    public void applyUserInitialChoices(Long userId, InitialChoiceRequest req) {
        Set<Long> chosen = req.distinctIds();
        if (chosen.size() < 5 || chosen.size() > 10) {
            throw new ValidationException("초기 전시는 5개 이상, 10개 이하로 선택해야 합니다.");
        }

        List<InitialExhibition> picked = initialExhibitionRepository.findAllById(chosen);
        if (picked.size() != chosen.size()) {
            throw new IllegalArgumentException("존재하지 않는 전시 ID가 포함되어 있습니다.");
        }

        UserGenre ug = userGenreRepository.findByUserId(userId)
                .orElseGet(() -> userGenreRepository.save(UserGenre.builder().userId(userId).build()));

        picked.forEach(ix -> ug.addFromExhibitionRecord(ix.getGenre(), ix.getExhibitionTheme()));

        userGenreRepository.save(ug);
    }
}
