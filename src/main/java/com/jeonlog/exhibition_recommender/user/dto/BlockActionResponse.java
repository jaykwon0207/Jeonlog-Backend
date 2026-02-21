package com.jeonlog.exhibition_recommender.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BlockActionResponse {

    private Long blockedUserId;
    private String blockedUserNickname;
    private boolean blocked;
    private List<Long> hiddenRecordIds;
}
