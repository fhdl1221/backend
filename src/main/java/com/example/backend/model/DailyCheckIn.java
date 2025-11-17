package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
// 1. UNIQUE(user_id, checkin_date) 제약 조건 설정
@Table(name = "daily_checkin", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "checkin_date"})
})
@Data
@NoArgsConstructor
public class DailyCheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkin_id")
    private Long id;

    // 2. User와의 관계 설정 (FK, ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // 3. DATE 타입은 LocalDate와 매핑
    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "stress_level", nullable = false)
    private Integer stressLevel; // DB에서 CHECK (1-5) 제약조건 관리

    @Column(name = "stress_emoji", length = 10)
    private String stressEmoji;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "dailyCheckin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StressCause> stressCauses;
}