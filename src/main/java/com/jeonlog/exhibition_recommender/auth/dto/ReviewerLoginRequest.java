package com.jeonlog.exhibition_recommender.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewerLoginRequest {
    private String reviewCode;
    private String password;

    public String getEffectiveReviewCode() {
        if (reviewCode != null && !reviewCode.isBlank()) {
            return reviewCode;
        }
        return password;
    }
}
