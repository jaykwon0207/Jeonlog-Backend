package com.jeonlog.exhibition_recommender.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationListResponse {
    private List<NotificationResponse> items;
    private Long nextCursor; // 다음 조회에 lastId로 사용 (없으면 null)
    private boolean hasNext;
}
