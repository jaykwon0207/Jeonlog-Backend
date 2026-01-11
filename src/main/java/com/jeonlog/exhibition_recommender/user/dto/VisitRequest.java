package com.jeonlog.exhibition_recommender.user.dto;


import lombok.Getter;

import java.time.LocalDate;

@Getter
public class VisitRequest {
    private LocalDate visitedAt;
}