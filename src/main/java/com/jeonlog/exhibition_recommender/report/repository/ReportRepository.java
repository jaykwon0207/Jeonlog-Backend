package com.jeonlog.exhibition_recommender.report.repository;

import com.jeonlog.exhibition_recommender.report.domain.Report;
import com.jeonlog.exhibition_recommender.report.domain.ReportAction;
import com.jeonlog.exhibition_recommender.report.domain.ReportStatus;
import com.jeonlog.exhibition_recommender.report.domain.ReportTargetType;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterAndTargetTypeAndTargetIdAndStatusIn(
            User reporter,
            ReportTargetType targetType,
            Long targetId,
            Collection<ReportStatus> statuses
    );

    List<Report> findAllByStatusOrderByCreatedAtAsc(ReportStatus status);

    List<Report> findAllByStatusAndDueAtBeforeOrderByCreatedAtAsc(ReportStatus status, LocalDateTime dueAt);

    List<Report> findAllByReportedUserAndStatusAndActionNotAndOffenderNoticeReadAtIsNullOrderByProcessedAtDesc(
            User reportedUser,
            ReportStatus status,
            ReportAction action
    );

    void deleteAllByReporterOrReportedUser(User reporter, User reportedUser);
}
