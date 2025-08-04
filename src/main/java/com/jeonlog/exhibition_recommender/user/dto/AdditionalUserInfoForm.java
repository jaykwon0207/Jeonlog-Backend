package com.jeonlog.exhibition_recommender.user.dto;

import com.jeonlog.exhibition_recommender.user.domain.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdditionalUserInfoForm {
    private Gender gender;
    private Integer birthYear;
}
