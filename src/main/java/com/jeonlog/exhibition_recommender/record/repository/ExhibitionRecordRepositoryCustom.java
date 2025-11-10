package com.jeonlog.exhibition_recommender.record.repository;

import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.record.dto.RecordSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExhibitionRecordRepositoryCustom {

    // 전시 제목 키워드와 해시태그 이름으로 전시 기록 동적 검색
    Page<ExhibitionRecord> search(String query, Pageable pageable);
}