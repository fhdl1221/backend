package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "routine") // 테이블명 지정
@Data
@NoArgsConstructor
public class RecoveryRoutine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routine_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    // 1. Enum 타입으로 Difficulty 매핑
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulty difficulty;

    @Column(length = 50)
    private String icon;

    @Column(name = "is_active")
    private boolean isActive = true; // 2. Java 기본값 설정

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 3. Difficulty Enum 정의
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRoutine> completions;
}