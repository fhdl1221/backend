package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CheckInRequest {
    private Integer stressLevel;
    private List<String> stressCauses; // 프론트에서 ["업무", "학업"] 형태의 문자열 리스트로 받음
    private String memo;
}