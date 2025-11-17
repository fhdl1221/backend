package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal; // 1. DECIMAL(3,2)는 BigDecimal로 매핑
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
// 2. UNIQUE(user_id, stat_date) 제약 조건 설정
@Table(name = "daily_statistics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "stat_date"})
})
@Data
@NoArgsConstructor
public class DailyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long id;

    // 3. User 관계 (FK, On Delete Cascade)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "avg_stress_level", precision = 3, scale = 2) // 4. DECIMAL(3,2) 매핑
    private BigDecimal avgStressLevel;

    @Column(name = "checkin_count")
    private int checkinCount = 0; // 5. Java 기본값 설정

    @Column(name = "content_view_count")
    private int contentViewCount = 0;

    @Column(name = "routine_complete_count")
    private int routineCompleteCount = 0;

    @Column(name = "chat_message_count")
    private int chatMessageCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}