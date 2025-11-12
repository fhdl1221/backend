package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users") // PostgreSQL에서는 'user'가 예약어일 수 있으므로 'users'로 변경
@Data // Lombok: Getter, Setter, ToString, EqualsAndHashCode,RequiredArgsConstructor
@NoArgsConstructor // Lombok: 기본 생성자
@AllArgsConstructor // Lombok: 모든 필드를 인자로 받는 생성자
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에 위임하여 ID 자동 생성

    private Long id;

    @Column(nullable = false, unique = true)
   private String username;
           @Column(nullable = false)
   private String password; // 비밀번호는 반드시 암호화되어 저장되어야합니다.
           @Column(nullable = false, unique = true)
   private String email;
           // 한 명의 유저는 여러 개의 메모를 가질 수 있습니다.
           // mappedBy는 Memo 엔티티의 'user' 필드에 의해 매핑됨을 나타냅니다.
           // CascadeType.ALL은 User가 삭제될 때 연관된 Memo도 함께 삭제되도록합니다.
   @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<Memo> memos;
   }