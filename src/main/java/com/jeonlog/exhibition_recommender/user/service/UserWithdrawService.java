package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.bookmark.repository.BookmarkRepository;
import com.jeonlog.exhibition_recommender.bookmark.repository.UserBookmarkRepository;
import com.jeonlog.exhibition_recommender.comment.repository.RecordCommentRepository;
import com.jeonlog.exhibition_recommender.like.repository.RecordLikeRepository;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.scrap.repository.RecordScrapRepository;
import com.jeonlog.exhibition_recommender.search.repository.SearchRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.FollowRepository;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.repository.UserVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWithdrawService {

    private final UserRepository userRepository;

    private final FollowRepository followRepository;

    private final ExhibitionRecordRepository recordRepository;
    private final RecordLikeRepository recordLikeRepository;
    private final RecordScrapRepository recordScrapRepository;
    private final RecordCommentRepository recordCommentRepository;

    private final BookmarkRepository bookmarkRepository;
    private final UserBookmarkRepository userBookmarkRepository;

    private final SearchRepository searchRepository;
    private final UserVisitRepository userVisitRepository;

    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow();

        // 1️⃣ 팔로우 관계
        followRepository.deleteAllByFollowerOrFollowing(user, user);

        // 2️⃣ 유저가 누른 좋아요 / 스크랩
        recordLikeRepository.deleteAllByUser(user);
        recordScrapRepository.deleteAllByUser(user);

        // 🔥 3️⃣ 유저가 작성한 댓글 (빠졌던 부분)
        recordCommentRepository.deleteAllByUser(user);

        // 4️⃣ 북마크 계열
        bookmarkRepository.deleteAllByUser(user);
        userBookmarkRepository.deleteAllByUser(user);

        // 5️⃣ 검색 / 방문 로그
        searchRepository.deleteAllByUser(user);
        userVisitRepository.deleteAllByUser(user);

        // 6️⃣ 유저가 작성한 기록
        List<ExhibitionRecord> records =
                recordRepository.findAllByUserOrderByCreatedAtDesc(user);

        if (!records.isEmpty()) {

            // 기록에 달린 자식 테이블 먼저 삭제
            recordCommentRepository.deleteAllByRecordIn(records);
            recordLikeRepository.deleteAllByRecordIn(records);
            recordScrapRepository.deleteAllByRecordIn(records);

            recordRepository.deleteAll(records);
        }

        // 7️⃣ 최종 유저 삭제
        userRepository.delete(user);
    }
}