package com.example.backend.repository;

import com.example.backend.model.RecoveryRoutine;
import com.example.backend.model.User;
import com.example.backend.model.UserRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoutineRepository extends JpaRepository<UserRoutine, Long> {

    // 특정 사용자의 모든 루틴 완료 기록 조회 (최신순)
    List<UserRoutine> findByUserOrderByCompletedAtDesc(User user);

    // 특정 루틴의 모든 완료 기록 조회
    List<UserRoutine> findByRoutine(RecoveryRoutine routine);
}