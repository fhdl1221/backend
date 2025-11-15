package com.example.backend.repository;

import com.example.backend.model.InitialSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InitialSurveyRepository extends JpaRepository<InitialSurvey, Long> {
}