package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users") // PostgreSQL에서는 'user'가 예약어일 수 있으므로 'users'로 변경
@Data // Lombok: Getter, Setter, ToString, EqualsAndHashCode,RequiredArgsConstructor
@NoArgsConstructor // Lombok: 기본 생성자
@AllArgsConstructor // Lombok: 모든 필드를 인자로 받는 생성자
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에 위임하여 ID 자동 생성
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password; // 비밀번호는 암호화되어 저장

    @Column(name = "industry", nullable = true, length = 50)
    private String industry;

    @Column(name = "career_years", nullable = true, length = 20)
    private String careerYears;

    @Column(name = "profile_image_url", nullable = true, length = 500)
    private String profileImageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    // 한 명의 유저는 여러 개의 메모를 가질 수 있습니다.
    // mappedBy는 Memo 엔티티의 'user' 필드에 의해 매핑됨을 나타냅니다.
    // CascadeType.ALL은 User가 삭제될 때 연관된 Memo도 함께 삭제되도록합니다.
    //   @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    //   private List<Memo> memos;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPreference userPreference;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private InitialSurvey initialSurvey;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyCheckIn> dailyCheckins;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedContent> savedContents;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentView> contentViews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatConversation> chatConversations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRoutine> userRoutines;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StressAlert> stressAlerts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyStatistics> dailyStatistics;
   }