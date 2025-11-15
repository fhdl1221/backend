package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "initial_survey") // 스키마의 테이블명
@Data
@NoArgsConstructor
public class InitialSurvey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // UNIQUE 제약조건
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "question_1", length = 10)
    private String question1;

    @Column(name = "question_2", length = 10)
    private String question2;

    @Column(name = "question_3", length = 10)
    private String question3;

    @Column(name = "question_4", length = 10)
    private String question4;

    @Column(name = "question_5", length = 10)
    private String question5;

    @Column(name = "stress_score", nullable = true)
    private Integer stressScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}