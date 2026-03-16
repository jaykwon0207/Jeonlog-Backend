package com.jeonlog.exhibition_recommender.notification.scheduler;

import com.jeonlog.exhibition_recommender.bookmark.repository.BookmarkRepository;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExhibitionEndingSoonScheduler {

    private final ExhibitionRepository exhibitionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NotificationService notificationService;

    // 매일 11:00 (서울 기준)
    @Scheduled(cron = "0 0 11 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "exhibitionEndingSoonScheduler_run", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void run() {
        LocalDate targetEndDate = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(14);

        // 종료일이 D+14인 전시들
        List<Exhibition> exhibitions = exhibitionRepository.findByEndDate(targetEndDate);

        String endDateIso = targetEndDate.toString(); // dedupKey용
        for (Exhibition ex : exhibitions) {
            // 알림 허용한( notifyEnabled=true ) 북마크 유저들만
            List<Long> userIds = bookmarkRepository.findNotifiedUserIdsByExhibitionId(ex.getId());

            for (Long userId : userIds) {
                notificationService.notifyExhibitionEndingSoon(
                        userId,
                        ex.getId(),
                        endDateIso,
                        ex.getTitle()
                );
            }
        }
    }
}
