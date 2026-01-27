package com.jeonlog.exhibition_recommender.notification.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ServiceAnnouncementCreateRequest {
    private String title;
    private String body;
    private boolean pushEnabled; // 중요 공지면 true

    private List<String> imageUrls;

}
