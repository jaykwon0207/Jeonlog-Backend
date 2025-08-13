package com.jeonlog.exhibition_recommender.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling   //endDate(전시 종료일) 지나면 북마트에서 자동으로 전시 삭제를 위한 config
public class SchedulerConfig {
}
