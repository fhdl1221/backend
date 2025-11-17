package com.example.backend.controller;

import com.example.backend.dto.CheckInRequest;
import com.example.backend.dto.CheckInResponse;
import com.example.backend.model.DailyCheckIn;
// [삭제] import com.example.backend.model.User;
import com.example.backend.service.DailyCheckInService;
// [삭제] import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // [추가] UserDetails 사용 예시
import org.springframework.security.core.userdetails.UsernameNotFoundException; // [추가]
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/check-in")
@RequiredArgsConstructor
public class DailyCheckInController {

    private final DailyCheckInService checkInService;

    /**
     * Spring Security의 Principal에서 이메일(Username)을 추출하는 헬퍼 메서드
     */
    private String getEmailFromPrincipal(Object principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }
        if (principal instanceof UserDetails) {
            // UserDetails 인터페이스를 사용하는 경우 (가장 일반적)
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            // Principal이 이메일 문자열 자체인 경우
            return (String) principal;
        }
        // 기타 JwtAuthenticationToken 등 다른 Principal 타입에 대한 처리가 필요할 수 있음
        throw new IllegalArgumentException("Cannot extract email from principal of type: " + principal.getClass());
    }

    /**
     * 오늘자 체크인 정보 조회
     */
    @GetMapping("/today")
    public ResponseEntity<CheckInResponse> getTodayCheckIn(@AuthenticationPrincipal Object principal) {
        try {
            String email = getEmailFromPrincipal(principal);
            Optional<DailyCheckIn> todayCheckIn = checkInService.getTodayCheckIn(email);

            return todayCheckIn
                    .map(checkIn -> ResponseEntity.ok(CheckInResponse.fromEntity(checkIn)))
                    .orElse(ResponseEntity.ok(null)); // 데이터가 없으면 null 반환 (프론트에서 처리)
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * 일일 체크인 생성
     */
    @PostMapping
    public ResponseEntity<?> createCheckIn(@RequestBody CheckInRequest request,
                                           @AuthenticationPrincipal Object principal) {
        try {
            String email = getEmailFromPrincipal(principal);
            DailyCheckIn newCheckIn = checkInService.createCheckIn(request, email);

            return new ResponseEntity<>(CheckInResponse.fromEntity(newCheckIn), HttpStatus.CREATED);

        } catch (IllegalStateException e) {
            // 이미 체크인한 경우 (409 Conflict)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (UsernameNotFoundException e) {
            // 사용자를 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // 인증 정보가 잘못된 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // 기타 예외
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("체크인 처리에 실패했습니다.");
        }
    }
}