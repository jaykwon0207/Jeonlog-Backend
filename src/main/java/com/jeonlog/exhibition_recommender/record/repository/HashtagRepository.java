package com.jeonlog.exhibition_recommender.record.repository;

import com.jeonlog.exhibition_recommender.record.domain.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    // 해시태그 이름으로 조회
    Optional<Hashtag> findByName(String name);

    // 여러 해시태그 이름을 한 번에 조회
    Set<Hashtag> findByNameIn(Set<String> names);
}