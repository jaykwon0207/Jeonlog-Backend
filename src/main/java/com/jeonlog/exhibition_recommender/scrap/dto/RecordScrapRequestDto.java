package com.jeonlog.exhibition_recommender.scrap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordScrapRequestDto {
    private Long recordId;
}
