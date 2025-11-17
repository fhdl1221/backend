package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_routine") // 테이블명 지정
@Data
@NoArgsConstructor
public class UserRoutine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_routine_id")
    private Long id;

    // 1. User 관계 (FK, On Delete Cascade)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // 2. Routine 관계 (FK, On Delete Cascade)
    // (이전 단계에서 생성한 Routine.java 엔티티를 참조합니다)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RecoveryRoutine routine;

    @CreationTimestamp // 3. DEFAULT CURRENT_TIMESTAMP 매핑
    @Column(name = "completed_at", nullable = false, updatable = false)
    private LocalDateTime completedAt;

    @Column(name = "rating") // DB에서 CHECK (1-5) 제약조건 관리
    private Integer rating;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;
}