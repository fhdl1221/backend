package com.example.backend.controller;

import com.example.backend.dto.DashboardDataResponse;
import com.example.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // DailyCheckInController에서 가져온 헬퍼 메서드
    private String getEmailFromPrincipal(Object principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        throw new IllegalArgumentException("Cannot extract email from principal of type: " + principal.getClass());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDataResponse> getDashboardData(
            @RequestParam(value = "period", defaultValue = "7") int period,
            @AuthenticationPrincipal Object principal) {

        try {
            String email = getEmailFromPrincipal(principal);
            DashboardDataResponse data = analyticsService.getDashboardData(email, period);
            return ResponseEntity.ok(data);
        } catch (UsernameNotFoundException e) {
            // 사용자를 찾을 수 없는 경우
            return ResponseEntity.status(404).build();
        } catch (IllegalArgumentException e) {
            // 인증 정보가 잘못된 경우
            return ResponseEntity.status(401).build();
        }
    }
}