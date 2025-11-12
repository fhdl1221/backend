package com.example.backend.dto;

import com.example.backend.model.Memo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemoResponse {
    private Long id;
    private String title;
    private String state;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String category;
    private LocalDateTime dueDate;

    public MemoResponse(Memo memo) {
        this.id = memo.getId();
        this.title = memo.getTitle();
        this.state = memo.getState();
        this.priority = memo.getPriority();
        this.createdAt = memo.getCreatedAt();
        this.updatedAt = memo.getUpdatedAt();
        this.category = memo.getCategory();
        this.dueDate = memo.getDueDate();
    }
}
