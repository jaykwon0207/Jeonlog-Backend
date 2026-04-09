package com.jeonlog.exhibition_recommender.report.service;

import com.jeonlog.exhibition_recommender.comment.repository.RecordCommentRepository;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.comment.domain.RecordComment;
import com.jeonlog.exhibition_recommender.like.repository.RecordLikeRepository;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.record.repository.RecordMediaRepository;
import com.jeonlog.exhibition_recommender.report.domain.Report;
import com.jeonlog.exhibition_recommender.report.domain.ReportAction;
import com.jeonlog.exhibition_recommender.report.domain.ReportReason;
import com.jeonlog.exhibition_recommender.report.domain.ReportStatus;
import com.jeonlog.exhibition_recommender.report.domain.ReportTargetType;
import com.jeonlog.exhibition_recommender.report.dto.AdminReportActionRequest;
import com.jeonlog.exhibition_recommender.report.dto.MyReportNoticeResponse;
import com.jeonlog.exhibition_recommender.report.dto.ReportAdminItemResponse;
import com.jeonlog.exhibition_recommender.report.dto.ReportCreateRequest;
import com.jeonlog.exhibition_recommender.report.dto.ReportCreateResponse;
import com.jeonlog.exhibition_recommender.report.repository.ReportRepository;
import com.jeonlog.exhibition_recommender.scrap.repository.RecordScrapRepository;
import com.jeonlog.exhibition_recommender.user.domain.Role;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final ExhibitionRecordRepository exhibitionRecordRepository;
    private final RecordCommentRepository recordCommentRepository;
    private final RecordLikeRepository recordLikeRepository;
    private final RecordScrapRepository recordScrapRepository;
    private final RecordMediaRepository recordMediaRepository;
    private final UserRepository userRepository;
    private final DiscordReportWebhookService discordReportWebhookService;

    @Transactional
    public ReportCreateResponse create(User reporter, ReportCreateRequest request) {
        log.info(
                "[REPORT] create_start reporterUserId={} targetType={} targetId={} reason={}",
                reporter.getId(),
                request.getTargetType(),
                request.getTargetId(),
                request.getReason()
        );
        validateReasonDetail(request.getReason(), request.getDetail());

        User reportedUser = resolveReportedUser(request.getTargetType(), request.getTargetId());

        if (reporter.getId().equals(reportedUser.getId())) {
            throw new IllegalArgumentException("자기 자신은 신고할 수 없습니다.");
        }

        boolean duplicated = reportRepository.existsByReporterAndTargetTypeAndTargetIdAndStatusIn(
                reporter,
                request.getTargetType(),
                request.getTargetId(),
                List.of(ReportStatus.OPEN, ReportStatus.IN_REVIEW)
        );
        if (duplicated) {
            throw new IllegalArgumentException("이미 접수되어 처리 중인 신고입니다.");
        }

        Report report = reportRepository.save(
                Report.builder()
                        .reporter(reporter)
                        .reportedUser(reportedUser)
                        .targetType(request.getTargetType())
                        .targetId(request.getTargetId())
                        .reason(request.getReason())
                        .detail(request.getDetail())
                        .build()
        );
        log.info(
                "[REPORT] created reportId={} reporterUserId={} reportedUserId={} targetType={} targetId={}",
                report.getId(),
                reporter.getId(),
                reportedUser.getId(),
                report.getTargetType(),
                report.getTargetId()
        );

        discordReportWebhookService.sendNewReport(report);
        return ReportCreateResponse.from(report);
    }

    public List<ReportAdminItemResponse> getAdminReports(User admin, ReportStatus status, boolean overdueOnly) {
        validateAdmin(admin);

        ReportStatus targetStatus = status == null ? ReportStatus.OPEN : status;
        List<Report> reports;
        if (overdueOnly) {
            reports = reportRepository.findAllByStatusAndDueAtBeforeOrderByCreatedAtAsc(targetStatus, LocalDateTime.now());
        } else {
            reports = reportRepository.findAllByStatusOrderByCreatedAtAsc(targetStatus);
        }
        return reports.stream().map(ReportAdminItemResponse::from).toList();
    }

    @Transactional
    public ReportAdminItemResponse act(User admin, Long reportId, AdminReportActionRequest request) {
        log.info(
                "[REPORT] action_start adminUserId={} reportId={} requestStatus={} requestAction={}",
                admin.getId(),
                reportId,
                request.getStatus(),
                request.getAction()
        );
        validateAdmin(admin);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        validateStatusAndAction(report.getTargetType(), request.getStatus(), request.getAction());

        if (request.getStatus() == ReportStatus.OPEN) {
            throw new IllegalArgumentException("OPEN 상태로는 변경할 수 없습니다.");
        }

        if (request.getStatus() == ReportStatus.IN_REVIEW) {
            report.review();
            log.info("[REPORT] status_changed reportId={} status=IN_REVIEW", reportId);
        } else {
            ReportAction appliedAction = request.getAction();
            if (request.getStatus() == ReportStatus.RESOLVED) {
                appliedAction = applyAction(report, request.getAction());
            } else {
                appliedAction = ReportAction.NONE;
            }
            report.resolve(request.getStatus(), appliedAction, request.getAdminMemo());
            log.info(
                    "[REPORT] resolved reportId={} status={} action={}",
                    reportId,
                    request.getStatus(),
                    appliedAction
            );
        }
        return ReportAdminItemResponse.from(report);
    }

    private void validateAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
    }

    public List<MyReportNoticeResponse> getMyPendingNotices(User me) {
        return reportRepository
                .findAllByReportedUserAndStatusAndActionNotAndOffenderNoticeReadAtIsNullOrderByProcessedAtDesc(
                        me,
                        ReportStatus.RESOLVED,
                        ReportAction.NONE
                )
                .stream()
                .map(MyReportNoticeResponse::from)
                .toList();
    }

    @Transactional
    public void markNoticeRead(User me, Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        if (!report.getReportedUser().getId().equals(me.getId())) {
            throw new AccessDeniedException("본인 제재 알림만 확인할 수 있습니다.");
        }
        if (report.getStatus() != ReportStatus.RESOLVED || report.getAction() == ReportAction.NONE) {
            throw new IllegalArgumentException("확인 처리할 제재 알림이 아닙니다.");
        }

        report.markOffenderNoticeRead();
    }

    private void validateReasonDetail(ReportReason reason, String detail) {
        if (reason == ReportReason.OTHER && (detail == null || detail.isBlank())) {
            throw new IllegalArgumentException("기타 사유는 상세 내용을 입력해야 합니다.");
        }
    }

    private User resolveReportedUser(ReportTargetType targetType, Long targetId) {
        return switch (targetType) {
            case RECORD -> exhibitionRecordRepository.findById(targetId)
                    .map(ExhibitionRecord::getUser)
                    .orElseThrow(() -> new IllegalArgumentException("신고 대상이 존재하지 않습니다."));
            case COMMENT -> recordCommentRepository.findById(targetId)
                    .map(RecordComment::getUser)
                    .orElseThrow(() -> new IllegalArgumentException("신고 대상이 존재하지 않습니다."));
            case USER -> userRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("신고 대상이 존재하지 않습니다."));
        };
    }

    private void validateStatusAndAction(ReportTargetType targetType, ReportStatus status, ReportAction action) {
        if (status == ReportStatus.IN_REVIEW && action != ReportAction.NONE) {
            throw new IllegalArgumentException("검토중(IN_REVIEW) 상태에서는 action은 NONE이어야 합니다.");
        }
        if (status == ReportStatus.REJECTED && action != ReportAction.NONE) {
            throw new IllegalArgumentException("기각(REJECTED) 상태에서는 action은 NONE이어야 합니다.");
        }
        if (status != ReportStatus.RESOLVED) {
            return;
        }

        if (targetType == ReportTargetType.USER) {
            if (!(action == ReportAction.NONE
                    || action == ReportAction.USER_WARNED
                    || action == ReportAction.USER_SUSPENDED_7_DAYS
                    || action == ReportAction.USER_SUSPENDED_30_DAYS
                    || action == ReportAction.USER_BANNED_PERMANENTLY)) {
                throw new IllegalArgumentException("유저 신고 처리에는 USER 계열 action만 사용할 수 있습니다.");
            }
            return;
        }

        if (!(action == ReportAction.NONE || action == ReportAction.CONTENT_DELETED)) {
            throw new IllegalArgumentException("게시물/댓글 신고 처리에는 CONTENT_DELETED 또는 NONE만 사용할 수 있습니다.");
        }
    }

    private ReportAction applyAction(Report report, ReportAction requestedAction) {
        if (requestedAction == ReportAction.NONE) {
            return ReportAction.NONE;
        }

        return switch (report.getTargetType()) {
            case RECORD -> {
                if (requestedAction != ReportAction.CONTENT_DELETED) {
                    throw new IllegalArgumentException("게시물 신고의 조치는 CONTENT_DELETED 또는 NONE만 가능합니다.");
                }
                ExhibitionRecord record = exhibitionRecordRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new IllegalArgumentException("삭제할 게시물을 찾을 수 없습니다."));
                recordCommentRepository.deleteAllByRecord(record);
                recordLikeRepository.deleteAllByRecord(record);
                recordScrapRepository.deleteAllByRecord(record);
                recordMediaRepository.deleteAllByRecordId(record.getId());
                exhibitionRecordRepository.delete(record);
                log.info("[REPORT] action_applied reportId={} action=CONTENT_DELETED targetType=RECORD targetId={}",
                        report.getId(), report.getTargetId());
                yield ReportAction.CONTENT_DELETED;
            }
            case COMMENT -> {
                if (requestedAction != ReportAction.CONTENT_DELETED) {
                    throw new IllegalArgumentException("댓글 신고의 조치는 CONTENT_DELETED 또는 NONE만 가능합니다.");
                }
                RecordComment comment = recordCommentRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new IllegalArgumentException("삭제할 댓글을 찾을 수 없습니다."));
                recordCommentRepository.delete(comment);
                log.info("[REPORT] action_applied reportId={} action=CONTENT_DELETED targetType=COMMENT targetId={}",
                        report.getId(), report.getTargetId());
                yield ReportAction.CONTENT_DELETED;
            }
            case USER -> applyUserProgressivePenalty(report.getReportedUser());
        };
    }

    private ReportAction applyUserProgressivePenalty(User reportedUser) {
        int strike = reportedUser.getModerationStrikeValue();
        if (strike <= 0) {
            reportedUser.warn();
            log.info("[REPORT] action_applied action=USER_WARNED reportedUserId={}", reportedUser.getId());
            return ReportAction.USER_WARNED;
        }
        if (strike == 1) {
            reportedUser.suspendForDays(7);
            log.info("[REPORT] action_applied action=USER_SUSPENDED_7_DAYS reportedUserId={}", reportedUser.getId());
            return ReportAction.USER_SUSPENDED_7_DAYS;
        }
        if (strike == 2) {
            reportedUser.suspendForDays(30);
            log.info("[REPORT] action_applied action=USER_SUSPENDED_30_DAYS reportedUserId={}", reportedUser.getId());
            return ReportAction.USER_SUSPENDED_30_DAYS;
        }
        reportedUser.banPermanently();
        log.info("[REPORT] action_applied action=USER_BANNED_PERMANENTLY reportedUserId={}", reportedUser.getId());
        return ReportAction.USER_BANNED_PERMANENTLY;
    }
}
