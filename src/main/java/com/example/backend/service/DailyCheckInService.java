package com.example.backend.service;

import com.example.backend.dto.CheckInRequest;
import com.example.backend.model.DailyCheckIn;
import com.example.backend.model.DailyStatistics; // [추가]
import com.example.backend.model.StressCause;
import com.example.backend.model.User;
import com.example.backend.repository.DailyCheckInRepository;
import com.example.backend.repository.DailyStatisticsRepository; // [추가]
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // [추가]
import org.slf4j.LoggerFactory; // [추가]
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // [추가]
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyCheckInService {

    // [추가] 로거
    private static final Logger logger = LoggerFactory.getLogger(DailyCheckInService.class);

    private final DailyCheckInRepository checkInRepository;
    private final UserRepository userRepository;
    private final DailyStatisticsRepository statsRepository; // [추가] DailyStatisticsRepository 주입

    /**
     * 오늘 날짜의 체크인 기록을 조회합니다.
     * @param userEmail (String) 현재 인증된 사용자의 이메일
     */
    @Transactional(readOnly = true)
    public Optional<DailyCheckIn> getTodayCheckIn(String userEmail) {
        // [수정] 이메일로 User 엔티티를 직접 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        LocalDate today = LocalDate.now();
        return checkInRepository.findByUserAndCheckinDate(user, today);
    }

    /**
     * 새로운 체크인을 생성합니다.
     * @param userEmail (String) 현재 인증된 사용자의 이메일
     */
    @Transactional
    public DailyCheckIn createCheckIn(CheckInRequest request, String userEmail) {
        // [수정] 이메일로 User 엔티티를 직접 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        LocalDate today = LocalDate.now();

        if (checkInRepository.findByUserAndCheckinDate(user, today).isPresent()) {
            throw new IllegalStateException("오늘은 이미 체크인했습니다.");
        }

        DailyCheckIn newCheckIn = new DailyCheckIn();
        newCheckIn.setUser(user);
        newCheckIn.setCheckinDate(today);
        newCheckIn.setStressLevel(request.getStressLevel());
        newCheckIn.setMemo(request.getMemo());
        newCheckIn.setStressEmoji(deriveEmoji(request.getStressLevel()));

        // [수정] StressCause 엔티티 리스트 생성 (causeType 필드 사용)
        List<StressCause> causeEntities = request.getStressCauses().stream()
                .map(causeString -> { // 프론트에서 받은 문자열 (예: "업무")
                    StressCause sc = new StressCause();
                    sc.setCauseType(causeString); // '.setCauseName()' -> '.setCauseType()'
                    sc.setDailyCheckin(newCheckIn);
                    return sc;
                })
                .collect(Collectors.toList());

        newCheckIn.setStressCauses(causeEntities);

        // [수정] DailyCheckIn 저장
        DailyCheckIn savedCheckIn = checkInRepository.save(newCheckIn);

        // --- ⬇️ [추가] DailyStatistics 업데이트 로직 ⬇️ ---
        try {
            // 1. 오늘 날짜의 통계 데이터가 이미 있는지 확인
            DailyStatistics stats = statsRepository.findByUserAndStatDate(user, today)
                    .orElse(new DailyStatistics()); // 없으면 새로 생성

            // 2. 통계 데이터 설정 (새로 생성된 경우 User와 Date 설정)
            if (stats.getId() == null) {
                stats.setUser(user);
                stats.setStatDate(today);
            }

            // 3. 통계 값 업데이트
            // "오늘은 이미 체크인했습니다" 예외 처리로 인해 이 로직은 하루에 한 번만 실행됨을 보장.
            stats.setCheckinCount(1); // 체크인 횟수 1로 설정
            stats.setAvgStressLevel(BigDecimal.valueOf(savedCheckIn.getStressLevel())); // 평균 스트레스(현재는 당일 값)

            // 4. 통계 저장
            statsRepository.save(stats);

        } catch (Exception e) {
            // (중요) 통계 업데이트가 실패하더라도 메인 기능인 체크인은 성공해야 하므로,
            // 여기서는 예외를 로깅만 하고 다시 던지지 않습니다.
            logger.error("Failed to update daily statistics for user: {}: {}", user.getEmail(), e.getMessage());
        }

        // [수정] 저장된 엔티티 반환
        return savedCheckIn;
    }

    private String deriveEmoji(Integer level) {
        if (level == null) return "😐";
        switch (level) {
            case 1: return "😊";
            case 2: return "🙂";
            case 3: return "😐";
            case 4: return "😟";
            case 5: return "😫";
            default: return "😐";
        }
    }
}