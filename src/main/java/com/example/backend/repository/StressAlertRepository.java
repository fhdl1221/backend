package com.example.backend.repository;

import com.example.backend.model.StressAlert;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StressAlertRepository extends JpaRepository<StressAlert, Long> {

    // 특정 사용자의 모든 알림 조회 (최신순)
    List<StressAlert> findByUserOrderByCreatedAtDesc(User user);

    // 특정 사용자의 읽음/안 읽음 알림 조회 (idx_user_alert 인덱스 활용)
    List<StressAlert> findByUserAndIsRead(User user, boolean isRead);
}