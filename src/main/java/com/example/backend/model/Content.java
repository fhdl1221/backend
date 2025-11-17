package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "content") // 스키마의 테이블명
@Data
@NoArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 1. Enum 타입으로 ContentType 매핑
    @Enumerated(EnumType.STRING) // DB에는 "VIDEO", "AUDIO" 문자열로 저장
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(length = 50)
    private String category;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "content_url", nullable = false, length = 500)
    private String contentUrl;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "view_count")
    private int viewCount = 0; // 2. Java 기본값 설정

    @Column(name = "is_active")
    private boolean isActive = true; // 3. Java 기본값 설정

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 4. ContentType Enum 정의 (별도 파일로 빼도 됩니다)
    public enum ContentType {
        VIDEO,
        AUDIO,
        TEXT
    }
}