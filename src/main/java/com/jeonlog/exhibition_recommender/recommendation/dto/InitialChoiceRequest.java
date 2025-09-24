package com.jeonlog.exhibition_recommender.recommendation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record InitialChoiceRequest(
        @NotNull
        @Min(5)  // 최소 5개
        @Max(10) // 최대 10개
        Integer count, // 선택 수 검증 보조
        @NotNull
        List<Long> ids // 사용자가 선택한 InitialExhibition ID 목록
) {
    public Set<Long> distinctIds() {
        return ids == null ? Set.of() : ids.stream().filter(i -> i != null).collect(Collectors.toSet());
    }
}
