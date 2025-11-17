package com.example.backend.repository;

import com.example.backend.model.DailyCheckIn;
import com.example.backend.model.StressCause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StressCauseRepository extends JpaRepository<StressCause, Long> {

    // 특정 체크인 ID에 속한 모든 원인 찾기
    List<StressCause> findByDailyCheckin(DailyCheckIn dailyCheckin);
}