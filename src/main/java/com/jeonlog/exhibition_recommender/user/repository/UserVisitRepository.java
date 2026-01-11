package com.jeonlog.exhibition_recommender.user.repository;

import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.domain.UserVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVisitRepository extends JpaRepository<UserVisit, Long> {
    List<UserVisit> findAllByUser(User user);

}
