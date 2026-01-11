package com.jeonlog.exhibition_recommender.recommendation.repository;

import com.jeonlog.exhibition_recommender.recommendation.domain.InitialExhibition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InitialExhibitionRepository extends JpaRepository<InitialExhibition, Long> {

    List<InitialExhibition> findTop20ByOrderByIdAsc();
}
