package com.example.backend.service;

import com.example.backend.dto.RoutineRecommendationResponse;
import com.example.backend.model.DailyCheckIn;
import com.example.backend.model.RecoveryRoutine;
import com.example.backend.model.StressCause;
import com.example.backend.model.User;
import com.example.backend.repository.DailyCheckInRepository;
import com.example.backend.repository.RecoveryRoutineRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final UserRepository userRepository;
    private final DailyCheckInRepository checkInRepository;
    private final RecoveryRoutineRepository routineRepository;

    private static final int ANALYSIS_DAYS = 7; // 분석 기간 (7일)

    @Transactional(readOnly = true)
    public RoutineRecommendationResponse getRecommendedRoutines(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // 1. 지난 7일간의 체크인 데이터 조회
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(ANALYSIS_DAYS -1); // 오늘 포함 7일
        List<DailyCheckIn> checkInList = checkInRepository.findByUserAndCheckinDateBetween(user, startDate, endDate);

        // [수정] 반환할 DTO 객체 생성
        RoutineRecommendationResponse response = new RoutineRecommendationResponse();

        if (checkInList.isEmpty()) {
            // 7일간 데이터가 없으면 기본 추천
            response.setAverageStress(BigDecimal.ZERO);
            response.setCheckInRate(0);
            response.setMainCause("데이터 없음");
            response.setPeakTime("N/A"); // [수정] 피크 시간 데이터 없음
            response.setAnalysisBasis(String.format("최근 %d일간의 체크인 기록이 없습니다.", ANALYSIS_DAYS));
            response.setRecommendedRoutines(routineRepository.findByCategory("BREATHING")); // 예: '호흡'
            return response;
        }

        // 2. [신규] 평균 스트레스 계산
        double stressSum = checkInList.stream()
                .mapToInt(DailyCheckIn::getStressLevel)
                .sum();
        BigDecimal avgStress = BigDecimal.valueOf(stressSum / checkInList.size())
                .setScale(1, RoundingMode.HALF_UP);
        response.setAverageStress(avgStress);

        // 3. [신규] 체크인 완료율 계산
        int checkInRate = (int) Math.round(((double) checkInList.size() / ANALYSIS_DAYS) * 100);
        response.setCheckInRate(checkInRate);

        // 2. 스트레스 원인 집계 (AnalyticsService와 유사)
        Map<String, Long> causeCounts = checkInList.stream()
                .flatMap(checkIn -> checkIn.getStressCauses().stream())
                .map(StressCause::getCauseType)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        long totalCauses = causeCounts.values().stream().mapToLong(Long::longValue).sum();

        // 3. 가장 빈번한 스트레스 원인 찾기
        String topCause = causeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("기타"); // 원인이 집계되지 않으면 '기타'

        // 5. [신규] 주요 원인 (비율 포함)
        int topCausePercentage = 0;
        if (totalCauses > 0) {
            topCausePercentage = (int) Math.round(((double) causeCounts.getOrDefault(topCause, 0L) / totalCauses) * 100);
        }
        response.setMainCause(String.format("%s (%d%%)", topCause, topCausePercentage));

        // 6. [신규] 피크 시간 (현재 DB 스키마로는 알 수 없으므로 Placeholder)
        response.setPeakTime("데이터 없음"); // DailyCheckIn에 시간 정보가 없음

        // 4. 원인을 루틴 카테고리로 매핑
        String routineCategory = mapCauseToRoutineCategory(topCause);

        // 5. 카테고리에 맞는 루틴 조회
        List<RecoveryRoutine> routines = routineRepository.findByCategory(routineCategory); //
        response.setRecommendedRoutines(routines);

        response.setAnalysisBasis(String.format(
                "지난 %d일간 '%s'이(가) 주요 스트레스 원인이었습니다.",
                ANALYSIS_DAYS,
                topCause
        ));

        return response;
    }

    /**
     * 스트레스 원인(String)을 루틴 카테고리(String)로 변환합니다.
     * (이 부분은 비즈니스 로직에 맞게 수정이 필요합니다)
     */
    private String mapCauseToRoutineCategory(String causeType) {
        // RecoveryRoutine의 category 필드 값과 일치해야 합니다.
        // 예: "업무 과다" -> "STRETCHING" (DB에 "STRETCHING" 카테고리가 있어야 함)
        switch (causeType) {
            case "업무 과다":
            case "마감일":
                return "STRETCHING"; // 예시 카테고리
            case "소통 문제":
            case "불안": // (CheckInRequest에 '불안'이 있다면)
                return "MEDITATION"; // 예시 카테고리
            case "회의":
                return "BREATHING"; // 예시 카테고리
            case "기타":
            default:
                return "WALKING"; // 예시 카테고리
        }
    }
}