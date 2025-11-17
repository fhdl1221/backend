package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "stress_cause") // 테이블명 지정 (스키마 기준)
@Data
@NoArgsConstructor
public class StressCause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cause_id")
    private Long id;

    // 1. DailyCheckin과의 관계 (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkin_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // 2. FK에 ON DELETE CASCADE 설정
    private DailyCheckIn dailyCheckin;

    @Column(name = "cause_type", nullable = false, length = 50)
    private String causeType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}