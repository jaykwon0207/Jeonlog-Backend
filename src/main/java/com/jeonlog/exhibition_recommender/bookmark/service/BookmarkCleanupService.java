package com.jeonlog.exhibition_recommender.bookmark.service;

import com.jeonlog.exhibition_recommender.bookmark.repository.UserBookmarkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BookmarkCleanupService {

    private final UserBookmarkRepository userBookmarkRepository;

    //매일 새벽 1시에 실행
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void removeExpiredBookmarks() {
        userBookmarkRepository.deleteBookmarksForEndedExhibitions(LocalDate.now());
    }
}
