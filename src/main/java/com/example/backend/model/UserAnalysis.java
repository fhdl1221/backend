package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_analysis")
@Data
@NoArgsConstructor
public class UserAnalysis {

    @Id
    private Long id; // User의 ID와 동일하게 사용

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // User의 ID를 이 테이블의 PK로 매핑
    @JoinColumn(name = "id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String conversationSummary; // 대화 요약

    private String currentSentiment; // 현재 감정 (예: "긍정", "부정", "중립")

    @UpdateTimestamp
    private LocalDateTime lastAnalyzed;

    public UserAnalysis(User user) {
        this.user = user;
    }
}