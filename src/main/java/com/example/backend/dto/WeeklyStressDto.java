package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // new WeeklyStressDto("월", 3.2)
public class WeeklyStressDto {
    private String day; // "월", "화", ...
    private double value; // 평균 스트레스 레벨
}