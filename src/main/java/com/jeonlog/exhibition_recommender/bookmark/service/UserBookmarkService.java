package com.jeonlog.exhibition_recommender.bookmark.service;

import com.jeonlog.exhibition_recommender.bookmark.dto.UserBookmarkDto;
import com.jeonlog.exhibition_recommender.bookmark.repository.UserBookmarkRepository;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.domain.UserBookmark;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBookmarkService {

    private final UserBookmarkRepository bookmarkRepository;
    private final ExhibitionRepository exhibitionRepository;

    public void addBookmark(Long exhibitionId, User user) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시가 존재하지 않습니다"));
        // 중복 체크
        if (bookmarkRepository.existsByUserAndExhibition(user, exhibition)) {
            throw new IllegalArgumentException("이미 찜한 전시입니다.");
        }

        // 저장
        UserBookmark bookmark = UserBookmark.builder()
                .user(user)
                .exhibition(exhibition)
                .bookmarkedAt(LocalDateTime.now())
                .notified(false)
                .build();

        bookmarkRepository.save(bookmark);
    }

    public void cancelBookmark(Long exhibitionId, User user) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시가 존재하지 않습니다."));

        UserBookmark bookmark = bookmarkRepository.findByUserAndExhibition(user, exhibition)
                .orElseThrow(() -> new IllegalArgumentException("찜을 누른 적이 없습니다."));

        bookmarkRepository.delete(bookmark);
    }

    public List<UserBookmarkDto> getBookmarksByUser(User user) {
        List<UserBookmark> bookmarks = bookmarkRepository.findAllByUser(user);

        return bookmarks.stream()
                .map(bookmark -> UserBookmarkDto.from(bookmark.getExhibition()))
                .toList();
    }

}
