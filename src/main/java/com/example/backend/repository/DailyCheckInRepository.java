package com.example.backend.repository;

import com.example.backend.model.DailyCheckIn;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCheckInRepository extends JpaRepository<DailyCheckIn, Long> {

    // (user_id, checkin_date)로 특정 체크인 조회 (Unique 제약조건)
    Optional<DailyCheckIn> findByUserAndCheckinDate(User user, LocalDate date);

    // 특정 사용자의 모든 체크인 기록 조회 (최신순)
    List<DailyCheckIn> findByUserOrderByCheckinDateDesc(User user);

    // 특정 기간 동안의 사용자 체크인 기록 조회
    List<DailyCheckIn> findByUserAndCheckinDateBetween(User user, LocalDate startDate, LocalDate endDate);
}