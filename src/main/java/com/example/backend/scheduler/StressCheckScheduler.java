package com.example.backend.scheduler;

import com.example.backend.dto.DashboardDataResponse;
import com.example.backend.dto.WeeklyStressDto;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AnalyticsService;
import com.example.backend.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StressCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(StressCheckScheduler.class);

    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;
    private final WebPushService webPushService;

    /**
     * [1. 정규 스케줄러]
     * 매일 오전 9시에 모든 사용자를 대상으로 실행됩니다.
     */
    @Scheduled(cron = "0 0 9 * * *") // cron 표현식 : 왼쪽부터 초, 분, 시, 일, 월, 요일
    @Transactional(readOnly = true)
    public void checkAllUsersStress() {
        logger.info("오전 9시 스트레스 알림 스케줄러 시작...");

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            // [수정] 분리된 로직 호출
            checkSingleUserStress(user.getEmail(), false);
        }
        logger.info("스트레스 알림 스케줄러 종료.");
    }

    /**
     * [2. 핵심 로직 (분리됨)]
     * 한 명의 사용자를 검사하고, 조건이 맞으면 알림을 전송합니다.
     * @param email 검사할 사용자의 이메일
     * @param isTestCall 테스트 API를 통해 호출되었는지 여부
     * @return 알림으로 보낸 메시지 내용 (또는 "조건 미충족")
     */
    @Transactional(readOnly = true)
    public String checkSingleUserStress(String email, boolean isTestCall) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            logger.warn("알림 로직 실패: 사용자를 찾을 수 없음 {}", email);
            return "사용자를 찾을 수 없습니다.";
        }

        LocalDate today = LocalDate.now();
        DayOfWeek todayOfWeek = today.getDayOfWeek();
        String todayKorean = todayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        // 1. 통계 데이터 조회
        DashboardDataResponse data = analyticsService.getDashboardData(user.getEmail(), 7);
        String notificationBody = null;

        // 2. [조건 1] 7일 평균 스트레스 4 이상
        if (data.getAverageStress().compareTo(BigDecimal.valueOf(4)) >= 0) {
            notificationBody = String.format(
                    "최근 7일간 평균 스트레스가 %.1f로 높습니다. 오늘 하루 마음을 챙겨보세요.",
                    data.getAverageStress()
            );
        }

        // 3. [조건 2] 오늘 요일 평균 스트레스 4 이상
        if (notificationBody == null) {
            for (WeeklyStressDto weeklyDto : data.getWeeklyStress()) {
                if (weeklyDto.getDay().equals(todayKorean) && weeklyDto.getValue() >= 4.0) {
                    notificationBody = String.format(
                            "최근 %s요일마다 스트레스가 높았습니다 (평균 %.1f). 오늘은 조금 천천히 가보시는 건 어떨까요?",
                            todayKorean,
                            weeklyDto.getValue()
                    );
                    break;
                }
            }
        }

        // 4. [테스트용 보정] 조건이 안 맞아도 테스트 호출이면 무조건 알림
        String title = "🧘 SoftDay 스트레스 알림";
        if (notificationBody == null && isTestCall) {
            title = "🧘 SoftDay 테스트 알림";
            notificationBody = String.format(
                    "테스트 알림입니다. (현재 평균 스트레스: %.1f, 오늘(%s) 평균: ...)",
                    data.getAverageStress(),
                    todayKorean
            );
        }

        // 5. 알림 발송
        if (notificationBody != null) {
            logger.info("알림 발송 대상: {} (이유: {})", user.getEmail(), notificationBody);

            String payloadJson = String.format(
                    "{\"title\": \"%s\", \"body\": \"%s\", \"url\": \"/statistics\"}",
                    title,
                    notificationBody.replace("\"", "\\\"") // JSON 이스케이프
            );

            webPushService.sendNotification(user, payloadJson);
            return notificationBody;
        }

        return "알림 조건 미충족";
    }
}