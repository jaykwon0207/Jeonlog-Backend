package com.jeonlog.exhibition_recommender.scrap.repository;

import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.scrap.domain.RecordScrap;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordScrapRepository extends JpaRepository<RecordScrap, Long> {

    // 특정 기록 + 유저 조합으로 스크랩 여부 확인
    Optional<RecordScrap> findByRecordAndUser(ExhibitionRecord record, User user);

    // 특정 유저의 전체 스크랩 목록 조회
    List<RecordScrap> findAllByUser(User user);

    // 특정 기록의 스크랩 개수
    Long countByRecord(ExhibitionRecord record);

    // 특정 기록의 스크랩 전체 삭제 (ex. 기록 삭제 시)
    void deleteAllByRecord(ExhibitionRecord record);

    // ✅ 탈퇴용 bulk 삭제 (추가)
    void deleteAllByRecordIn(List<ExhibitionRecord> records);

    void deleteAllByUser(User user);


}
