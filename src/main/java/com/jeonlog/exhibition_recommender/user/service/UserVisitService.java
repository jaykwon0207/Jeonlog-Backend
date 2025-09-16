package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.domain.UserVisit;
import com.jeonlog.exhibition_recommender.user.dto.VisitRequest;
import com.jeonlog.exhibition_recommender.user.dto.VisitedExhibitionDto;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.repository.UserVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserVisitService {

    private final UserRepository userRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final UserVisitRepository userVisitRepository;

    // 방문 기록 저장
    public void recordVisit(Long exhibitionId, String email, VisitRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시를 찾을 수 없습니다."));

        LocalDate visitedAt = (request != null && request.getVisitedAt() != null)
                ? request.getVisitedAt()
                : LocalDate.now();

        UserVisit visit = UserVisit.builder()
                .user(user)
                .exhibition(exhibition)
                .visitedAt(visitedAt)
                .build();

        userVisitRepository.save(visit);
    }

    // 내가 방문한 전시 목록 조회
    public List<VisitedExhibitionDto> getVisitedExhibitions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<UserVisit> visits = userVisitRepository.findAllByUser(user);

        return visits.stream()
                .map(userVisit -> {
                    Exhibition e = userVisit.getExhibition();
                    return VisitedExhibitionDto.builder()
                            .id(e.getId())
                            .title(e.getTitle())
                            .location(e.getLocation())
                            .startDate(e.getStartDate() != null ? e.getStartDate().toString() : null)
                            .endDate(e.getEndDate() != null ? e.getEndDate().toString() : null)
                            .posterUrl(e.getPosterUrl())
                            .build();
                })
                .collect(Collectors.toList()); // ✅ Java 11 호환
    }
}