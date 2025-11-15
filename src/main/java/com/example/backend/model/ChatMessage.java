package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String sender; // "USER" 또는 "AI"

    @Column(nullable = false, columnDefinition = "TEXT") // 긴 텍스트
    private String message;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    public ChatMessage(User user, String sender, String message) {
        this.user = user;
        this.sender = sender;
        this.message = message;
    }
}