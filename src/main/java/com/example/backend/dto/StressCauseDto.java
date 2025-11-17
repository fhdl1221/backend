package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StressCauseDto {
    private String name;  // "업무 과다"
    private int value; // 35 (%)
    private String color; // "#F59E0B"
}