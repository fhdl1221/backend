package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardDataResponse {
    // 1. 요약 카드
    private BigDecimal averageStress;
    private int comparisonPercentage; // 지난 기간 대비 % (구현 편의상 0으로 고정)
    private int checkInCount;
    private int totalDays;

    // 2. 차트 데이터
    private List<DailyStressDto> dailyStress; // 일별 스트레스 추이 (라인 차트)
    private List<WeeklyStressDto> weeklyStress; // 요일별 평균 스트레스 (바 차트)

    // 3. 원인 분석
    private List<StressCauseDto> stressCauses; // 주요 스트레스 원인 (백분율)

    // 4. 콘텐츠 시청 (요청에 따라 빈 리스트로 반환)
    private List<?> contentViews;
}