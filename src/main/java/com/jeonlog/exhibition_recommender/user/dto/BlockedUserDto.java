package com.jeonlog.exhibition_recommender.user.dto;

import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BlockedUserDto {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime blockedAt;

    public static BlockedUserDto of(User user, LocalDateTime blockedAt) {
        return BlockedUserDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .blockedAt(blockedAt)
                .build();
    }
}
