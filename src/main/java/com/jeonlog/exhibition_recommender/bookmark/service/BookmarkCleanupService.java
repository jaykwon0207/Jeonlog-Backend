package com.jeonlog.exhibition_recommender.bookmark.service;

import com.jeonlog.exhibition_recommender.bookmark.repository.UserBookmarkRepository;
import jakarta.transaction.Transactional;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class BookmarkCleanupService {

    private final UserBookmarkRepository userBookmarkRepository;

    public BookmarkCleanupService(UserBookmarkRepository userBookmarkRepository) {
        this.userBookmarkRepository = userBookmarkRepository;
    }

    //매일 새벽 1시에 실행
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "bookmarkCleanupService_removeExpiredBookmarks", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    @Transactional
    public void removeExpiredBookmarks() {
        LocalDate todayInSeoul = LocalDate.now(ZoneId.of("Asia/Seoul"));
        userBookmarkRepository.deleteBookmarksForEndedExhibitions(todayInSeoul);
    }
}
