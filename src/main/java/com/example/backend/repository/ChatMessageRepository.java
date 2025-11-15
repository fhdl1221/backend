package com.example.backend.repository;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 유저의 최근 대화 N개를 시간순으로 조회 (최신순 -> 역순 정렬)
    List<ChatMessage> findTop10ByUserOrderByTimestampDesc(User user);
}