package com.jeonlog.exhibition_recommender.bookmark.service;

import com.jeonlog.exhibition_recommender.bookmark.domain.Bookmark;
import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkRequest;
import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkResponse;
import com.jeonlog.exhibition_recommender.bookmark.dto.ExhibitionDto;
import com.jeonlog.exhibition_recommender.bookmark.repository.BookmarkRepository;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
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
    private final UserGenreRepository userGenreRepository;

    // 북마크 추가
    @Transactional
    public BookmarkResponse add(Long exhibitionId, String email) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));



        Bookmark bm = bookmarkRepository.findByUserAndExhibition(me, ex)
                .orElseGet(() -> {
                    // 신규 생성 시 가중치 +0.02
                    Bookmark created = bookmarkRepository.save(
                            Bookmark.builder().user(me).exhibition(ex).build()
                    );

                    UserGenre ug = userGenreRepository.findByUserId(me.getId())
                            .orElseGet(() -> userGenreRepository.save(
                                    UserGenre.builder().userId(me.getId()).build()
                            ));
                    ug.addFromBookmark(ex.getGenre(), ex.getExhibitionTheme());
                    return created;
                });

        long count = bookmarkRepository.countByExhibition(ex);
        return BookmarkResponse.of(ex, true, bm.isNotifyEnabled(), count);
    }

    // 알림 상태 변경
    @Transactional
    public BookmarkResponse updateNotify(Long exhibitionId, String email, BookmarkRequest req) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));

        Bookmark bm = bookmarkRepository.findByUserAndExhibition(me, ex)
                .orElseThrow(() -> new IllegalStateException("북마크가 존재하지 않습니다."));

        bm.updateNotify(req.isNotifyEnabled());
        long count = bookmarkRepository.countByExhibition(ex);
        return BookmarkResponse.of(ex, true, bm.isNotifyEnabled(), count);
    }

    // 북마크 삭제
    @Transactional
    public BookmarkResponse remove(Long exhibitionId, String email) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));

        bookmarkRepository.findByUserAndExhibition(me, ex).ifPresent(bm -> {
            bookmarkRepository.delete(bm);

            userGenreRepository.findByUserId(me.getId()).ifPresent(ug ->
                    ug.revertBookmark(ex.getGenre(), ex.getExhibitionTheme())
            );
        });

        long count = bookmarkRepository.countByExhibition(ex);
        return BookmarkResponse.of(ex, false, false, count);
    }

    // 전시별 북마크 수
    public Long count(Long exhibitionId) {
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));
        return bookmarkRepository.countByExhibition(ex);
    }

    // 내가 북마크한 전시 목록
    @Transactional(readOnly = true)
    public Page<ExhibitionDto> listMyBookmarks(String email, Pageable pageable) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return bookmarkRepository.findByUser(me, pageable)
                .map(bm -> toExhibitionDto(bm.getExhibition()));
    }

    // 내 북마크 상태 조회
    @Transactional(readOnly = true)
    public BookmarkResponse myBookmarkState(Long exhibitionId, String email) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Exhibition ex = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("전시 없음"));

        return bookmarkRepository.findByUserAndExhibition(me, ex)
                .map(bm -> BookmarkResponse.of(ex, true, bm.isNotifyEnabled(),
                        bookmarkRepository.countByExhibition(ex)))
                .orElse(BookmarkResponse.of(ex, false, false,
                        bookmarkRepository.countByExhibition(ex)));
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
                .exhibitionMood(ex.getExhibitionTheme() != null ? ex.getExhibitionTheme().name() : null)
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