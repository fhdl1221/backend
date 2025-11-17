package com.example.backend.repository;

import com.example.backend.model.Content;
import com.example.backend.model.ContentView;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContentViewRepository extends JpaRepository<ContentView, Long> {

    // 특정 사용자의 모든 시청 기록 조회 (최신순)
    List<ContentView> findByUserOrderByViewedAtDesc(User user);

    // 특정 사용자가 특정 콘텐츠를 시청한 모든 기록 조회
    List<ContentView> findByUserAndContent(User user, Content content);

    // 특정 기간 동안의 사용자 시청 기록 조회
    List<ContentView> findByUserAndViewedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    // 특정 사용자의 완료된 시청 기록 조회
    List<ContentView> findByUserAndIsCompleted(User user, boolean isCompleted);
}