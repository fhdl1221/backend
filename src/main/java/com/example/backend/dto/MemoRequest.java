package com.example.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemoRequest {
    private String title;
    private String state;
    private String priority;
    private String category;
    private LocalDateTime dueDate;
}
