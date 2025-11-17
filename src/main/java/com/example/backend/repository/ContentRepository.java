package com.example.backend.repository;

import com.example.backend.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    // 콘텐츠 유형(Type)으로 검색
    List<Content> findByContentType(Content.ContentType contentType);

    // 카테고리로 검색
    List<Content> findByCategory(String category);

    // 카테고리와 유형으로 동시 검색
    List<Content> findByCategoryAndContentType(String category, Content.ContentType contentType);
}