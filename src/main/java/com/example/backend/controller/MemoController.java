package com.example.backend.controller;

import com.example.backend.dto.MemoRequest;
import com.example.backend.dto.MemoResponse;
import com.example.backend.model.Memo;
import com.example.backend.service.MemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/memos")
public class MemoController {

    @Autowired
    private MemoService memoService;

    // 현재 로그인한 사용자의 모든 메모 조회
    @GetMapping
    public ResponseEntity<List<MemoResponse>> getMemos(Authentication authentication) {
        String username = authentication.getName();
        List<Memo> memos = memoService.getMemosForUser(username);
        List<MemoResponse> memoResponses = memos.stream().map(MemoResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(memoResponses);
    }

    // 새 메모 생성
    @PostMapping
    public ResponseEntity<MemoResponse> createMemo(@RequestBody MemoRequest memoRequest, Authentication authentication) {
        String username = authentication.getName();
        Memo newMemo = memoService.createMemo(memoRequest, username);
        return ResponseEntity.ok(new MemoResponse(newMemo));
    }

    // 특정 메모 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateMemo(@PathVariable Long id, @RequestBody MemoRequest memoRequest, Authentication authentication) {
        try {
            String username = authentication.getName();
            Memo updatedMemo = memoService.updateMemo(id, memoRequest, username);
            return ResponseEntity.ok(new MemoResponse(updatedMemo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 특정 메모 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMemo(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            memoService.deleteMemo(id, username);
            return ResponseEntity.ok("Memo deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
