package com.example.backend.repository;

import com.example.backend.model.Memo;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // 스프링 빈으로 등록
public interface MemoRepository extends JpaRepository<Memo, Long> { // 특정 유저의 모든 메모를 찾는 메서드
    List<Memo> findByUser(User user);

    // 특정 유저의 특정 상태 메모를 찾는 메서드
    List<Memo> findByUserAndState(User user, String state);
}
