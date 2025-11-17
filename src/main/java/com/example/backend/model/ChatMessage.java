package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message") // 테이블명 지정 (스키마 기준)
@Data
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    // 1. [수정] User -> ChatConversation 으로 ManyToOne 관계 변경
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatConversation chatConversation;

    // 2. [수정] Enum 타입으로 Role 매핑 (user, assistant)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 3. [삭제] 기존 생성자 (User user, ...) 제거 (필요시 새 생성자 추가)
    public ChatMessage(ChatConversation chatConversation, MessageRole role, String content) {
        this.chatConversation = chatConversation;
        this.role = role;
        this.content = content;
    }

    // 4. MessageRole Enum 정의 (user, assistant)
    public enum MessageRole {
        USER,
        ASSISTANT
    }
}