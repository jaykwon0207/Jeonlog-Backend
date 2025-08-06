package com.jeonlog.exhibition_recommender.exhibition.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionClickLog;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionClickLogDto;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionClickLogRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExhibitionClickLogService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionClickLogRepository clickLogRepository;


    // 전시 클릭 로그 저장 (하루에 한 번만 저장됨)
    public void saveClick(Long exhibitionId, User user, ExhibitionClickLogDto dto) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new EntityNotFoundException("해당 전시를 찾을 수 없습니다."));

        LocalDateTime clickedAt = LocalDateTime.now();
        LocalDate clickedDate = clickedAt.toLocalDate();

        boolean alreadyClicked = clickLogRepository
                .findByUserAndExhibitionAndClickedDate(user, exhibition, clickedDate)
                .isPresent();

        if (!alreadyClicked) {
            ExhibitionClickLog log = ExhibitionClickLog.builder()
                    .user(user)
                    .exhibition(exhibition)
                    .clickedDate(clickedDate)
                    .build();

            clickLogRepository.save(log);
        }
    }

    // 성별별 클릭 통계 조회
    public Map<String, Object> getClickStatsByGender() {
        List<Object[]> results = clickLogRepository.findClickStatsByGender();
        Map<String, Object> stats = new HashMap<>();
        
        for (Object[] result : results) {
            String title = (String) result[0];
            Gender gender = (Gender) result[1];
            Long clickCount = (Long) result[2];
            
            if (!stats.containsKey(title)) {
                stats.put(title, new HashMap<String, Long>());
            }
            ((Map<String, Long>) stats.get(title)).put(gender.name(), clickCount);
        }
        
        return stats;
    }

    // 연령대별 클릭 통계 조회
    public Map<String, Object> getClickStatsByAgeGroup() {
        List<Object[]> results = clickLogRepository.findClickStatsByAgeGroup();
        Map<String, Object> stats = new HashMap<>();
        
        for (Object[] result : results) {
            String title = (String) result[0];
            String ageGroup = (String) result[1];
            Long clickCount = (Long) result[2];
            
            if (!stats.containsKey(title)) {
                stats.put(title, new HashMap<String, Long>());
            }
            ((Map<String, Long>) stats.get(title)).put(ageGroup, clickCount);
        }
        
        return stats;
    }

    // 특정 전시회의 성별별 클릭 통계 조회
    public Map<String, Long> getClickStatsByGenderForExhibition(Long exhibitionId) {
        // 전시회 존재 여부 확인
        if (!exhibitionRepository.existsById(exhibitionId)) {
            throw new EntityNotFoundException("해당 전시를 찾을 수 없습니다.");
        }
        
        List<Object[]> results = clickLogRepository.findClickStatsByGenderForExhibition(exhibitionId);
        Map<String, Long> stats = new HashMap<>();
        
        for (Object[] result : results) {
            Gender gender = (Gender) result[0];
            Long clickCount = (Long) result[1];
            stats.put(gender.name(), clickCount);
        }
        
        return stats;
    }
}
