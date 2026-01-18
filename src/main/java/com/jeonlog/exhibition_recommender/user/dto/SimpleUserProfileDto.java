package com.jeonlog.exhibition_recommender.user.dto;

import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimpleUserProfileDto {

    private Long userId;
    private String name;
    private String profileImageUrl;
    private String introduction;
    private String nickname;
    private int postCount;
    private int followerCount;
    private int followingCount;
    private boolean isFollowing;

    public static SimpleUserProfileDto from(User user, boolean isFollowing,
                                            int postCount, int followerCount, int followingCount) {
        return SimpleUserProfileDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .introduction(user.getIntroduction())
                .nickname(user.getNickname())
                .postCount(postCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }
}