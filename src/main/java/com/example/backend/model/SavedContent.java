package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
// 1. UNIQUE(user_id, content_id) 제약 조건 설정
@Table(name = "saved_content", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "content_id"})
})
@Data
@NoArgsConstructor
public class SavedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saved_id")
    private Long id;

    // 2. User 관계 (FK, On Delete Cascade)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // 3. Content 관계 (FK, On Delete Cascade)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Content content;

    @Column(name = "is_viewed")
    private boolean isViewed = false; // 4. DEFAULT FALSE 설정

    @CreationTimestamp
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;
}