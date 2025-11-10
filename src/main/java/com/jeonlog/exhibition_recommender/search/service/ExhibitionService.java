package com.jeonlog.exhibition_recommender.search.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.search.dto.ExhibitionSearchResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionDetailResponseDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;

    // 전체 전시 목록 조회
    public List<ExhibitionResponseDto> getAllExhibitions() {
        return exhibitionRepository.findAll().stream()
                .map(ExhibitionResponseDto::from)
                .collect(Collectors.toList());
    }

    // 전체 전시 목록 상세 조회
    public List<ExhibitionResponseDto> getAllExhibitionsDetails() {
        return exhibitionRepository.findAllWithVenue()
                .stream()
                .map(ExhibitionResponseDto::from)
                .collect(Collectors.toList());
    }

    // 전시 상세 조회
    public ExhibitionDetailResponseDto getExhibitionDetailById(Long id) {
        Exhibition exhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 전시가 존재하지 않습니다."));

        return ExhibitionDetailResponseDto.from(exhibition);
    }


    // 전시 검색 (query와 filter 조건에 따라 title, artist, genre, location 기반 필터링)
    public List<ExhibitionSearchResponseDto> searchExhibitions(
            String query,
            List<String> filter
    ) {
        List<Exhibition> exhibitions = exhibitionRepository.findAll();
        List<String> keywords = processQuery(query);

        return exhibitions.stream()
                .filter(e -> {
                    if (!keywords.isEmpty() && filter != null && !filter.isEmpty()) {
                        boolean match = false;

                        for (String f : filter) {
                            switch (f) {
                                case "title":
                                    if (keywords.stream().anyMatch(k ->
                                            e.getTitle().toLowerCase().contains(k.toLowerCase()))) match = true;
                                    break;
                                case "artist":
                                    if (e.getArtists() != null && e.getArtists().stream()
                                            .anyMatch(a -> keywords.stream()
                                                    .anyMatch(k -> a.getName().toLowerCase().contains(k.toLowerCase())))) match = true;
                                    break;
                                case "genre":
                                    if (e.getExhibitionGenres() != null && e.getExhibitionGenres().stream()
                                            .anyMatch(g -> keywords.stream()
                                                    .anyMatch(k -> g.getGenre().getGenreType().name().toLowerCase().contains(k.toLowerCase())))) match = true;
                                    break;
                                case "location":
                                    // venue.name + location 같이 검색
                                    if (e.getVenue() != null && keywords.stream().anyMatch(k ->
                                            (e.getVenue().getName() + " " + e.getLocation())
                                                    .toLowerCase()
                                                    .contains(k.toLowerCase()))) match = true;
                                    break;
                                default:
                                    throw new IllegalArgumentException("유효하지 않은 filter 값입니다: " + f);
                            }
                        }

                        if (!match) return false;
                    } else if (!keywords.isEmpty()) {
                        // filter가 없으면 전체 필드에서 검색
                        boolean match = keywords.stream().anyMatch(k ->
                                e.getTitle().toLowerCase().contains(k.toLowerCase()) ||
                                        (e.getVenue() != null && (e.getVenue().getName() + " " + e.getLocation()).toLowerCase().contains(k.toLowerCase())) ||
                                        (e.getArtists() != null && e.getArtists().stream()
                                                .anyMatch(a -> a.getName().toLowerCase().contains(k.toLowerCase()))) ||
                                        (e.getExhibitionGenres() != null && e.getExhibitionGenres().stream()
                                                .anyMatch(g -> g.getGenre().getGenreType().name().toLowerCase().contains(k.toLowerCase())))
                        );

                        if (!match) return false;
                    }

                    return true;
                })
                .map(e -> ExhibitionSearchResponseDto.builder()
                        .id(e.getId())
                        .title(e.getTitle())
                        .artist(e.getArtists() != null && !e.getArtists().isEmpty() ? e.getArtists().get(0).getName() : null)
                        .location(e.getVenue() != null ? e.getVenue().getName() + " " + e.getLocation() : e.getLocation()) // 응답에도 venue + location
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .posterUrl(e.getPosterUrl())
                        .price(e.getPrice())
                        .build()
                )
                .collect(Collectors.toList());
    }

    // 검색 쿼리를 키워드 리스트로 가공 (정규화 및 필터링 포함)
    private List<String> processQuery(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(query.split("\\s+"))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(this::normalizeKeyword)
                .filter(word -> word.length() >= 1)
                .distinct()
                .collect(Collectors.toList());
    }

    // 키워드 정규화 처리 (소문자화 및 특수문자 제거)
    private String normalizeKeyword(String keyword) {
        return keyword.toLowerCase()
                .replaceAll("[^a-zA-Z0-9가-힣\\s]", "")
                .trim();
    }
}
