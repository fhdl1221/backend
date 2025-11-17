package com.example.backend.repository;

import com.example.backend.model.ChatConversation;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    // 특정 사용자의 모든 대화 목록 조회 (최신순)
    List<ChatConversation> findByUserOrderByUpdatedAtDesc(User user);
}