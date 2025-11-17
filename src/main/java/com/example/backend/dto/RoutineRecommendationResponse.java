package com.example.backend.dto;

import com.example.backend.model.RecoveryRoutine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutineRecommendationResponse {
    // 1. [수정] "지난주 분석 결과" 카드용 데이터
    private BigDecimal averageStress;
    private String peakTime; // "피크 시간"
    private String mainCause; // "주요 원인 (비율%)"
    private int checkInRate; // "체크인 완료율"

    // "지난 7일간 '업무 과다'가 주요 스트레스 원인이었습니다."
    private String analysisBasis;

    // 분석을 기반으로 추천된 루틴 목록
    private List<RecoveryRoutine> recommendedRoutines;
}