package com.jeonlog.exhibition_recommender.bookmark.service;

import com.jeonlog.exhibition_recommender.bookmark.domain.Bookmark;
import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkRequest;
import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkResponse;
import com.jeonlog.exhibition_recommender.bookmark.dto.ExhibitionDto;
import com.jeonlog.exhibition_recommender.bookmark.repository.BookmarkRepository;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final UserRepository userRepository;

    // 북마크 추가 (notifyEnabled 포함)
    @Transactional
    public BookmarkResponse add(Long exhibitionId, String email, BookmarkRequest req) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));

        boolean notify = req != null && req.isNotifyEnabled();

        // 이미 있으면 알림 상태만 최신화
        Bookmark bm = bookmarkRepository.findByUserIdAndExhibitionId(me.getId(), ex.getId())
                .map(existing -> {
                    existing.updateNotify(notify);
                    return existing;
                })
                .orElseGet(() -> bookmarkRepository.save(
                        Bookmark.builder().user(me).exhibition(ex).notifyEnabled(notify).build()
                ));

        long count = bookmarkRepository.countByExhibitionId(exhibitionId);
        return BookmarkResponse.of(ex, true, bm.isNotifyEnabled(), count);
    }

    // 알림 상태만 변경
    @Transactional
    public BookmarkResponse updateNotify(Long exhibitionId, String email, BookmarkRequest req) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));

        Bookmark bm = bookmarkRepository.findByUserIdAndExhibitionId(me.getId(), ex.getId())
                .orElseThrow(() -> new IllegalStateException("북마크가 존재하지 않습니다."));

        bm.updateNotify(req.isNotifyEnabled());
        long count = bookmarkRepository.countByExhibitionId(exhibitionId);
        return BookmarkResponse.of(ex, true, bm.isNotifyEnabled(), count);
    }

    // 북마크 삭제
    @Transactional
    public BookmarkResponse remove(Long exhibitionId, String email) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));

        bookmarkRepository.deleteByUserIdAndExhibitionId(me.getId(), exhibitionId);
        long count = bookmarkRepository.countByExhibitionId(exhibitionId);
        return BookmarkResponse.of(ex, false, false, count);
    }

    // 전시별 북마크 개수 조회
    public Long count(Long exhibitionId) {
        return bookmarkRepository.countByExhibitionId(exhibitionId);
    }


    // 내가 북마크한 전시 목록 (DTO 변환)
    @Transactional(readOnly = true)
    public Page<ExhibitionDto> listMyBookmarks(String email, Pageable pageable) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        return bookmarkRepository.findByUser(me, pageable)
                .map(bm -> toExhibitionDto(bm.getExhibition()));
    }

    // 내 북마크 상태 조회 (bookmarked + notifyEnabled)
    @Transactional(readOnly = true)
    public BookmarkResponse myBookmarkState(Long exhibitionId, String email) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));

        return bookmarkRepository.findByUserIdAndExhibitionId(me.getId(), exhibitionId)
                .map(bm -> BookmarkResponse.of(ex, true, bm.isNotifyEnabled(),
                        bookmarkRepository.countByExhibitionId(exhibitionId)))
                .orElse(BookmarkResponse.of(ex, false, false,
                        bookmarkRepository.countByExhibitionId(exhibitionId)));
    }

    private ExhibitionDto toExhibitionDto(Exhibition ex) {
        Venue v = ex.getVenue();
        return ExhibitionDto.builder()
                .id(ex.getId())
                .title(ex.getTitle())
                .description(ex.getDescription())
                .location(ex.getLocation())
                .posterUrl(ex.getPosterUrl())
                .startDate(ex.getStartDate() != null ? ex.getStartDate().toString() : null)
                .endDate(ex.getEndDate() != null ? ex.getEndDate().toString() : null)
                .price(ex.getPrice())
                .exhibitionMood(ex.getExhibitionMood() != null ? ex.getExhibitionMood().name() : null)
                .venue(v == null ? null : ExhibitionDto.VenueDto.builder()
                        .id(v.getId())
                        .name(v.getName())
                        .type(v.getType() != null ? v.getType().name() : null)
                        .address(v.getAddress())
                        .latitude(v.getLatitude().doubleValue())
                        .longitude(v.getLongitude().doubleValue())
                        .build())
                .build();
    }
}