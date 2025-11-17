package com.example.backend.repository;

import com.example.backend.model.DailyStatistics;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatisticsRepository extends JpaRepository<DailyStatistics, Long> {

    // 특정 사용자의 특정 날짜 통계 조회 (UNIQUE 제약조건 활용)
    Optional<DailyStatistics> findByUserAndStatDate(User user, LocalDate date);

    // 특정 사용자의 날짜 범위별 통계 조회 (idx_user_stat_date 인덱스 활용)
    List<DailyStatistics> findByUserAndStatDateBetweenOrderByStatDateDesc(User user, LocalDate startDate, LocalDate endDate);
}