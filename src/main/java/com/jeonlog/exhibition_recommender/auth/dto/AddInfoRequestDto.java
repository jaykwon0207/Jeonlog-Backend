package com.jeonlog.exhibition_recommender.auth.dto;

import com.jeonlog.exhibition_recommender.user.domain.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddInfoRequestDto {
    private Gender gender;
    private Integer birthYear;
    private String nickname;

}
