package com.jeonlog.exhibition_recommender.exhibition.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameContainingIgnoreCase(String namePart);
}


