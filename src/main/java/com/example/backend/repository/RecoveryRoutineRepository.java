package com.example.backend.repository;

import com.example.backend.model.RecoveryRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecoveryRoutineRepository extends JpaRepository<RecoveryRoutine, Long> {

    // 카테고리로 루틴 검색
    List<RecoveryRoutine> findByCategory(String category);

    // 난이도로 루틴 검색
    List<RecoveryRoutine> findByDifficulty(RecoveryRoutine.Difficulty difficulty);
}