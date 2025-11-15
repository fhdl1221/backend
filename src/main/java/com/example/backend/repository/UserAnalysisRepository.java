package com.example.backend.repository;

import com.example.backend.model.User;
import com.example.backend.model.UserAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAnalysisRepository extends JpaRepository<UserAnalysis, Long> {

    // 유저 객체로 분석 내용 찾기
    Optional<UserAnalysis> findByUser(User user);
}