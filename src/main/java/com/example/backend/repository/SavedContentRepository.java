package com.example.backend.repository;

import com.example.backend.model.Content;
import com.example.backend.model.SavedContent;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedContentRepository extends JpaRepository<SavedContent, Long> {

    // 특정 사용자가 저장한 모든 콘텐츠 조회 (최신순)
    List<SavedContent> findByUserOrderBySavedAtDesc(User user);

    // 특정 사용자가 특정 콘텐츠를 저장했는지 확인 (UNIQUE 제약조건 활용)
    Optional<SavedContent> findByUserAndContent(User user, Content content);

    // 특정 사용자가 저장한 콘텐츠 중 시청/미시청 목록 조회
    List<SavedContent> findByUserAndIsViewed(User user, boolean isViewed);
}