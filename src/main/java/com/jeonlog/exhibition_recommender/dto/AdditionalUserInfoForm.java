package com.jeonlog.exhibition_recommender.dto;

import com.jeonlog.exhibition_recommender.domain.user.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdditionalUserInfoForm {
    private Gender gender;
    private Integer birthYear;
}
