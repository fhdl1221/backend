package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_view") // 테이블명 지정
@Data
@NoArgsConstructor
public class ContentView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_id")
    private Long id;

    // 1. User 관계 (FK, On Delete Cascade)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // 2. Content 관계 (FK, On Delete Cascade)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Content content;

    @Column(name = "watch_duration_seconds")
    private Integer watchDurationSeconds;

    @Column(name = "is_completed")
    private boolean isCompleted = false; // 3. DEFAULT FALSE 설정

    @CreationTimestamp
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt;
}