package com.jeonlog.exhibition_recommender.scrap.service;

import com.jeonlog.exhibition_recommender.common.metric.Action;
import com.jeonlog.exhibition_recommender.common.metric.CountView;
import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.repository.ExhibitionRecordRepository;
import com.jeonlog.exhibition_recommender.scrap.domain.RecordScrap;
import com.jeonlog.exhibition_recommender.scrap.dto.RecordScrapResponseDto;
import com.jeonlog.exhibition_recommender.scrap.repository.RecordScrapRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecordScrapService {

    private final RecordScrapRepository recordScrapRepository;
    private final ExhibitionRecordRepository exhibitionRecordRepository;


     // 스크랩 등록 또는 취소 (토글 방식)

    public boolean toggleScrap(Long recordId, User user) {
        ExhibitionRecord record = exhibitionRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시기록을 찾을 수 없습니다."));

        // 이미 스크랩 되어 있는지 확인
        return recordScrapRepository.findByRecordAndUser(record, user)
                .map(existingScrap -> { // 이미 있으면 삭제
                    recordScrapRepository.delete(existingScrap);
                    return false; // false = 스크랩 취소됨
                })
                .orElseGet(() -> { // 없으면 새로 추가
                    recordScrapRepository.save(
                            RecordScrap.builder()
                                    .record(record)
                                    .user(user)
                                    .build()
                    );
                    return true; // true = 새로 스크랩됨
                });
    }


    //  특정 사용자의 스크랩 목록 조회
    @Transactional(readOnly = true)
    public List<RecordScrap> getUserScraps(User user) {
        return recordScrapRepository.findAllByUser(user);
    }


     // 특정 기록의 스크랩 수 조회
    @Transactional(readOnly = true)
    public long countScraps(Long recordId) {
        ExhibitionRecord record = exhibitionRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 전시기록을 찾을 수 없습니다."));
        return recordScrapRepository.countByRecord(record);
    }


     // 토글 + 개수 조회를 한 번에 처리하는 편의 메서드

    @Transactional
    @CountView(type = "record", idExpr = "#recordId", action = Action.SAVE,
            condition = "#result.scrapped")
    public RecordScrapResponseDto toggleScrapWithCount(Long recordId, User user) {
        boolean scrapped = toggleScrap(recordId, user);
        long count = countScraps(recordId);
        return RecordScrapResponseDto.builder()
                .scrapped(scrapped)
                .scrapCount(count)
                .message(scrapped ? "스크랩 성공" : "스크랩 취소")
                .build();
    }
}
