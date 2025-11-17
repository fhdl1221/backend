package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor // new DailyStressDto("11월 17일", 3.5)
public class DailyStressDto {
    private String date; // "11월 17일"
    private BigDecimal value; // 스트레스 레벨
}