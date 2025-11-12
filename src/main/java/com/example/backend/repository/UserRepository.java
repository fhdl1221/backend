package com.example.backend.repository;

import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository // 스프링 빈으로 등록
public interface UserRepository extends JpaRepository<User, Long> { // 사용자 이름으로 User를 찾는 메서드 (Spring Security에서 사용될예정)
    Optional<User> findByUsername(String username);
    // 이메일로 User를 찾는 메서드
    Optional<User> findByEmail(String email);

    // 사용자 이름 존재 여부 확인
    boolean existsByUsername(String username);

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
}
