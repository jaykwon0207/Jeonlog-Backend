
package com.jeonlog.exhibition_recommender.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {
    private String introduction;
    private String profileImageUrl;
    private String nickname;
}