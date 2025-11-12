package com.example.backend.service;

import com.example.backend.dto.MemoRequest;
import com.example.backend.model.Memo;
import com.example.backend.model.User;
import com.example.backend.repository.MemoRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoService {

    @Autowired
    private MemoRepository memoRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Memo createMemo(MemoRequest memoRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Memo memo = new Memo();
        memo.setTitle(memoRequest.getTitle());
        memo.setState(memoRequest.getState());
        memo.setPriority(memoRequest.getPriority());
        memo.setUser(user);
        memo.setCategory(memoRequest.getCategory());
        memo.setDueDate(memoRequest.getDueDate());

        return memoRepository.save(memo);
    }

    @Transactional(readOnly = true)
    public List<Memo> getMemosForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return memoRepository.findByUser(user);
    }

    @Transactional
    public Memo updateMemo(Long memoId, MemoRequest memoRequest, String username) {
        Memo memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("Memo not found with id: " + memoId));

        // 메모의 소유자인지 확인
        if (!memo.getUser().getUsername().equals(username)) {
            throw new RuntimeException("User not authorized to update this memo");
        }

        memo.setTitle(memoRequest.getTitle());
        memo.setState(memoRequest.getState());
        memo.setPriority(memoRequest.getPriority());
        memo.setCategory(memoRequest.getCategory());
        memo.setDueDate(memoRequest.getDueDate());

        return memoRepository.save(memo);
    }

    @Transactional
    public void deleteMemo(Long memoId, String username) {
        Memo memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("Memo not found with id: " + memoId));

        // 메모의 소유자인지 확인
        if (!memo.getUser().getUsername().equals(username)) {
            throw new RuntimeException("User not authorized to delete this memo");
        }

        memoRepository.delete(memo);
    }
}
