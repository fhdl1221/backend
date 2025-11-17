package com.example.backend.service;

import com.example.backend.dto.DailyStressDto;
import com.example.backend.dto.DashboardDataResponse;
import com.example.backend.dto.StressCauseDto;
import com.example.backend.dto.WeeklyStressDto;
import com.example.backend.model.DailyCheckIn;
import com.example.backend.model.DailyStatistics;
import com.example.backend.model.StressCause;
import com.example.backend.model.User;
import com.example.backend.repository.DailyCheckInRepository;
import com.example.backend.repository.DailyStatisticsRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final DailyStatisticsRepository statsRepository;
    private final DailyCheckInRepository checkInRepository; // StressCause 집계를 위해 필요

    // 프론트 Mock 데이터와 동일한 색상 매핑
    private static final Map<String, String> CAUSE_COLORS = Map.of(
            "업무 과다", "#F59E0B",
            "회의", "#EF4444",
            "마감일", "#8B5CF6",
            "소통 문제", "#3B82F6",
            "기타", "#10B981"
    );
    private static final String DEFAULT_COLOR = "#6B7280"; // 잿빛

    @Transactional(readOnly = true)
    public DashboardDataResponse getDashboardData(String email, int period) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(period - 1);

        // 1. DailyStatistics 데이터 조회 (요약 카드 + 일별 차트)
        List<DailyStatistics> statsList = statsRepository.findByUserAndStatDateBetweenOrderByStatDateDesc(user, startDate, endDate);

        // 2. DailyCheckIn 데이터 조회 (요일별 차트 + 원인 분석)
        // (DailyCheckInRepository에 findByUserAndCheckinDateBetween이 정의되어 있어야 함)
        List<DailyCheckIn> checkInList = checkInRepository.findByUserAndCheckinDateBetween(user, startDate, endDate);

        DashboardDataResponse response = new DashboardDataResponse();
        response.setTotalDays(period);

        // 3. 요약 카드 및 일별 차트 데이터 집계 (statsList 사용)
        populateSummaryAndDailyChart(response, statsList);

        // 4. 요일별 차트 및 원인 분석 집계 (checkInList 사용)
        populateWeeklyChartAndCauses(response, checkInList);

        // 5. 콘텐츠 시청 (요청대로 빈 리스트 반환)
        response.setContentViews(Collections.emptyList());

        // 6. 지난 기간 대비 (일단 0으로 설정)
        response.setComparisonPercentage(0); // TODO: 지난 기간 (startDate-period ~ startDate-1) 데이터 조회 후 비교 로직 필요

        return response;
    }

    private void populateSummaryAndDailyChart(DashboardDataResponse response, List<DailyStatistics> statsList) {
        int totalCheckIns = 0;
        BigDecimal stressSum = BigDecimal.ZERO;
        int stressCount = 0;
        List<DailyStressDto> dailyStressData = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M월 d일");

        for (DailyStatistics stat : statsList) {
            totalCheckIns += stat.getCheckinCount();

            BigDecimal avgStress = stat.getAvgStressLevel();
            if (avgStress != null && avgStress.compareTo(BigDecimal.ZERO) > 0) {
                stressSum = stressSum.add(avgStress);
                stressCount++;
            }

            // 차트 데이터 추가 (날짜 포맷)
            String formattedDate = stat.getStatDate().format(dateFormatter);
            dailyStressData.add(new DailyStressDto(formattedDate, avgStress != null ? avgStress : BigDecimal.ZERO));
        }

        response.setCheckInCount(totalCheckIns);

        // 평균 스트레스 계산
        if (stressCount > 0) {
            BigDecimal avgStress = stressSum.divide(new BigDecimal(stressCount), 2, RoundingMode.HALF_UP);
            response.setAverageStress(avgStress);
        } else {
            response.setAverageStress(BigDecimal.ZERO);
        }

        // 차트는 시간순(오름차순)이어야 하므로 역순 정렬
        Collections.reverse(dailyStressData);
        response.setDailyStress(dailyStressData);
    }

    private void populateWeeklyChartAndCauses(DashboardDataResponse response, List<DailyCheckIn> checkInList) {
        // [요일, [스트레스 합계, 카운트]]
        Map<DayOfWeek, double[]> weeklyStressMap = new EnumMap<>(DayOfWeek.class);
        // [원인 타입, 카운트]
        Map<String, Long> causeCounts = new HashMap<>();
        long totalCauses = 0;

        for (DailyCheckIn checkIn : checkInList) {
            // 1. 요일별 스트레스 집계
            DayOfWeek day = checkIn.getCheckinDate().getDayOfWeek();
            double[] stats = weeklyStressMap.computeIfAbsent(day, k -> new double[2]);
            stats[0] += checkIn.getStressLevel(); // 합계
            stats[1]++; // 카운트

            // 2. 원인별 집계
            if (checkIn.getStressCauses() != null) {
                for (StressCause cause : checkIn.getStressCauses()) {
                    causeCounts.merge(cause.getCauseType(), 1L, Long::sum);
                    totalCauses++;
                }
            }
        }

        // 요일별 차트 DTO 생성 (월~일 순서 고정)
        List<WeeklyStressDto> weeklyStressData = new ArrayList<>();
        List<DayOfWeek> daysInOrder = List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        );

        for (DayOfWeek day : daysInOrder) {
            double[] stats = weeklyStressMap.getOrDefault(day, new double[2]);
            double avg = (stats[1] > 0) ? (stats[0] / stats[1]) : 0.0;
            // "월", "화" 등으로 변환
            String dayName = day.getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            weeklyStressData.add(new WeeklyStressDto(dayName, avg));
        }
        response.setWeeklyStress(weeklyStressData);

        // 원인 분석 DTO 생성 (백분율 계산)
        long finalTotalCauses = totalCauses;
        List<StressCauseDto> stressCauseData = causeCounts.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    long count = entry.getValue();
                    int percentage = (finalTotalCauses > 0) ? (int) Math.round(((double) count / finalTotalCauses) * 100) : 0;
                    String color = CAUSE_COLORS.getOrDefault(name, DEFAULT_COLOR);
                    return new StressCauseDto(name, percentage, color);
                })
                .sorted((c1, c2) -> Integer.compare(c2.getValue(), c1.getValue())) // 높은 순 정렬
                .collect(Collectors.toList());

        response.setStressCauses(stressCauseData);
    }
}