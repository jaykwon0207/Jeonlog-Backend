package com.jeonlog.exhibition_recommender.search.service;

import com.jeonlog.exhibition_recommender.common.metric.Action;
import com.jeonlog.exhibition_recommender.common.metric.CountView;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionDetailResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.search.dto.ExhibitionSearchResponseDto;
import com.jeonlog.exhibition_recommender.user.domain.Role;
import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;

    // 전체 전시 목록 조회 (간단 응답용)
    public List<ExhibitionResponseDto> getAllExhibitions() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return exhibitionRepository.findByEndDateGreaterThanEqual(today).stream()
                .map(ExhibitionResponseDto::from)
                .collect(Collectors.toList());
    }

    // 전체 전시 목록 상세 조회 (Venue join 포함)
    public List<ExhibitionResponseDto> getAllExhibitionsDetails() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return exhibitionRepository.findAllWithVenueByEndDateGreaterThanEqual(today).stream()
                .map(ExhibitionResponseDto::from)
                .collect(Collectors.toList());
    }

    // 전시 상세 조회
    @CountView(type = "exhibition", idExpr = "#id", action = Action.VIEW)
    public ExhibitionDetailResponseDto getExhibitionDetailById(Long id) {
        Exhibition exhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 전시가 존재하지 않습니다."));
        return ExhibitionDetailResponseDto.from(exhibition);
    }

    // 전시 검색 (phrase-only / 통째 검색)
    // - query를 띄어쓰기 단위로 쪼개지 않음
    // - 공백/빈 문자열이면 전체 반환 X -> 예외 발생
    // - filter가 있으면 해당 필드들에서만 phrase 포함 여부 체크
    // - filter가 없으면 title/artist/genre/location 전체 필드에서 phrase 포함 여부 체크
    public List<ExhibitionSearchResponseDto> searchExhibitions(String query, List<String> filter) {

        // 1) 검색어 정규화: 소문자화 + 특수문자 제거 + 공백 정리
        String phrase = normalizeText(query);

        // 2) 공백/빈 검색어면 전체 전시 반환하면 안됨 -> 예외 처리
        if (phrase.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        // 3) 현재 구조: 전체 전시를 메모리에서 필터링
        //    (데이터 많아지면 DB 검색으로 바꾸는 게 정석)
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<Exhibition> exhibitions = exhibitionRepository.findByEndDateGreaterThanEqual(today);

        // 4) filter 존재 여부
        boolean hasFilter = filter != null && !filter.isEmpty();

        // 5) 조건에 맞는 전시만 DTO로 변환해서 반환
        return exhibitions.stream()
                .filter(e -> matchByPhrase(e, phrase, filter, hasFilter))
                .map(this::toSearchResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePosterUrl(Long exhibitionId, String posterUrl, User user) {
        validateAdmin(user);
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시를 찾을 수 없습니다."));
        exhibition.updatePosterUrl(posterUrl);
        exhibitionRepository.save(exhibition);
    }

    // phrase 매칭 로직(핵심)
    // - hasFilter=true  : filter에 포함된 필드에서만 검사
    // - hasFilter=false : 전체 필드에서 검사
    private boolean matchByPhrase(Exhibition e, String phrase, List<String> filter, boolean hasFilter) {

        // filter가 있으면: 해당 필드만 체크
        if (hasFilter) {
            boolean match = false;

            for (String f : filter) {
                switch (f) {
                    case "title":
                        match |= containsNormalized(e.getTitle(), phrase);
                        break;

                    case "artist":
                        if (e.getArtists() != null) {
                            match |= e.getArtists().stream()
                                    .anyMatch(a -> containsNormalized(a.getName(), phrase));
                        }
                        break;

                    case "genre":
                        if (e.getExhibitionGenres() != null) {
                            match |= e.getExhibitionGenres().stream()
                                    .anyMatch(g -> {
                                        String genre = (g.getGenre() != null)
                                                ? g.getGenre().getGenreType().name()
                                                : null;
                                        return containsNormalized(genre, phrase);
                                    });
                        }
                        break;

                    case "location":
                        // venue.name + location 합쳐서 통째 검색
                        String loc = buildLocationText(e);
                        match |= containsNormalized(loc, phrase);
                        break;

                    default:
                        // 유효하지 않은 filter 값이면 예외
                        throw new IllegalArgumentException("유효하지 않은 filter 값입니다: " + f);
                }
            }

            return match;
        }

        // filter가 없으면: 전체 필드에서 체크
        String loc = buildLocationText(e);

        boolean titleMatch = containsNormalized(e.getTitle(), phrase);
        boolean locMatch = containsNormalized(loc, phrase);

        boolean artistMatch = e.getArtists() != null && e.getArtists().stream()
                .anyMatch(a -> containsNormalized(a.getName(), phrase));

        boolean genreMatch = e.getExhibitionGenres() != null && e.getExhibitionGenres().stream()
                .anyMatch(g -> {
                    String genre = (g.getGenre() != null)
                            ? g.getGenre().getGenreType().name()
                            : null;
                    return containsNormalized(genre, phrase);
                });

        return titleMatch || locMatch || artistMatch || genreMatch;
    }

    // location 텍스트 구성 (venue.name + location)
    // - venue/location이 null일 수 있으니 안전 처리
    private String buildLocationText(Exhibition e) {
        String venueName = (e.getVenue() != null && e.getVenue().getName() != null)
                ? e.getVenue().getName()
                : "";
        String location = (e.getLocation() != null) ? e.getLocation() : "";
        return (venueName + " " + location).trim();
    }

    // 정규화된 포함 여부 검사
    // - fieldValue(원문)도 정규화 후 phrase 포함 여부 확인
    // - null/blank는 false
    private boolean containsNormalized(String fieldValue, String phrase) {
        if (phrase == null || phrase.isBlank()) return false;
        if (fieldValue == null || fieldValue.isBlank()) return false;
        return normalizeText(fieldValue).contains(phrase);
    }

    // 검색 응답 DTO 변환
    private ExhibitionSearchResponseDto toSearchResponseDto(Exhibition e) {
        return ExhibitionSearchResponseDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .artist(e.getArtists() != null && !e.getArtists().isEmpty()
                        ? e.getArtists().get(0).getName()
                        : null)
                .location(e.getVenue() != null
                        ? e.getVenue().getName() + " " + e.getLocation()
                        : e.getLocation())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .posterUrl(e.getPosterUrl())
                .price(e.getPrice())
                .build();
    }

    // 문자열 정규화
    // - 소문자화
    // - 특수문자 제거(한글/영문/숫자/공백만 남김)
    // - trim
    // - 여러 공백을 1개로 통일
    private String normalizeText(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9가-힣\\s]", "")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private void validateAdmin(User user) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
    }
}
