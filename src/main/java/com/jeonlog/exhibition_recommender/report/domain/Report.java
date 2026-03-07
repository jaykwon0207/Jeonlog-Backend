package com.jeonlog.exhibition_recommender.report.domain;

import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ugc_reports",
        indexes = {
                @Index(name = "idx_ugc_reports_status_created_at", columnList = "status,created_at"),
                @Index(name = "idx_ugc_reports_status_due_at", columnList = "status,due_at"),
                @Index(name = "idx_ugc_reports_target", columnList = "target_type,target_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporter_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ugc_reports_reporter"))
    private User reporter;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reported_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ugc_reports_reported_user"))
    private User reportedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;

    @Column(length = 1000)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportAction action;

    @Column(name = "admin_memo", length = 1000)
    private String adminMemo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "offender_notice_read_at")
    private LocalDateTime offenderNoticeReadAt;

    @Builder
    public Report(
            User reporter,
            User reportedUser,
            ReportTargetType targetType,
            Long targetId,
            ReportReason reason,
            String detail
    ) {
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.detail = detail;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (dueAt == null) {
            dueAt = createdAt.plusHours(24);
        }
        if (status == null) {
            status = ReportStatus.OPEN;
        }
        if (action == null) {
            action = ReportAction.NONE;
        }
    }

    public void review() {
        this.status = ReportStatus.IN_REVIEW;
    }

    public void resolve(ReportStatus status, ReportAction action, String adminMemo) {
        this.status = status;
        this.action = action;
        this.adminMemo = adminMemo;
        this.processedAt = LocalDateTime.now();
    }

    public void markOffenderNoticeRead() {
        this.offenderNoticeReadAt = LocalDateTime.now();
    }
}
