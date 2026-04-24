package com.jeonlog.exhibition_recommender.exhibition.service;

import com.jeonlog.exhibition_recommender.common.metric.Action;
import com.jeonlog.exhibition_recommender.common.metric.CountView;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionClickLog;
import com.jeonlog.exhibition_recommender.exhibition.dto.CategoryCountDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionClickLogDto;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionClickLogRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExhibitionClickLogService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionClickLogRepository clickLogRepository;

    @Transactional
    @CountView(type = "exhibition", idExpr = "#exhibitionId", action = Action.VIEW)
    public SavedClick saveClick(Long exhibitionId, User user, ExhibitionClickLogDto dto) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new EntityNotFoundException("해당 전시를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDate clickedDate = now.toLocalDate();

        boolean alreadyClicked = clickLogRepository
                .findByUserAndExhibitionAndClickedDate(user, exhibition, clickedDate)
                .isPresent();

        if (!alreadyClicked) {
            ExhibitionClickLog log = ExhibitionClickLog.builder()
                    .user(user)
                    .exhibition(exhibition)
                    .clickedDate(clickedDate)
                    .build();

            ExhibitionClickLog saved = clickLogRepository.save(log);
            Instant clickedAtInstant = now.toInstant(java.time.ZoneOffset.UTC);

            return new SavedClick(saved.getId(), clickedAtInstant);
        }

        return new SavedClick(null, now.toInstant(java.time.ZoneOffset.UTC));
    }

    @Transactional(readOnly = true)
    public List<CategoryCountDto> getClickStatsByAgeGroupForExhibition(Long exhibitionId) {
        if (!exhibitionRepository.existsById(exhibitionId)) {
            throw new EntityNotFoundException("해당 전시를 찾을 수 없습니다.");
        }

        List<Object[]> results = clickLogRepository.findClickStatsByAgeGroupForExhibition(exhibitionId);
        Map<String, Long> temp = new HashMap<>();
        for (Object[] row : results) {
            String ageGroup = (String) row[0];
            Long count = (Long) row[1];
            temp.put(ageGroup, count);
        }

        List<String> order = Arrays.asList("10s", "20s", "30s", "40s", "50s", "60s+");
        List<CategoryCountDto> list = new ArrayList<>();
        for (String key : order) {
            list.add(new CategoryCountDto(key, temp.getOrDefault(key, 0L)));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<CategoryCountDto> getClickStatsByGenderForExhibition(Long exhibitionId) {
        if (!exhibitionRepository.existsById(exhibitionId)) {
            throw new EntityNotFoundException("해당 전시를 찾을 수 없습니다.");
        }

        List<Object[]> results = clickLogRepository.findClickStatsByGenderForExhibition(exhibitionId);
        Map<String, Long> temp = new HashMap<>();
        for (Object[] row : results) {
            Gender gender = (Gender) row[0];
            Long count = (Long) row[1];
            temp.put(gender.name(), count);
        }

        List<String> order = Arrays.asList("MALE", "FEMALE", "UNKNOWN");
        List<CategoryCountDto> list = new ArrayList<>();
        for (String key : order) {
            list.add(new CategoryCountDto(key, temp.getOrDefault(key, 0L)));
        }
        return list;
    }

    public static class SavedClick {
        private final Long logId;
        private final Instant clickedAt;
        public SavedClick(Long logId, Instant clickedAt) {
            this.logId = logId;
            this.clickedAt = clickedAt;
        }
        public Long getLogId() { return logId; }
        public Instant getClickedAt() { return clickedAt; }
    }
}