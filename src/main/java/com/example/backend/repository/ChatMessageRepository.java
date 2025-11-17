package com.example.backend.repository;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.ChatConversation; // 1. User -> ChatConversation
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 2. [수정] 특정 대화 ID에 속한 모든 메시지를 시간순으로 조회
    List<ChatMessage> findByChatConversationOrderByCreatedAtAsc(ChatConversation chatConversation);

    // 3. [수정] 특정 대화 ID의 최근 N개 메시지를 최신순으로 조회
    List<ChatMessage> findTop10ByChatConversationOrderByCreatedAtDesc(ChatConversation chatConversation);
}