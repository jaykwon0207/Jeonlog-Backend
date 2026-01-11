package com.jeonlog.exhibition_recommender.exhibition.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class CategoryCountDto {
    private String key;
    private Long count;
}
