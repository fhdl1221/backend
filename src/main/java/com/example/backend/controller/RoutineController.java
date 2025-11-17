package com.example.backend.controller;

import com.example.backend.dto.RoutineRecommendationResponse;
import com.example.backend.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineService routineService;

    // DailyCheckInController의 헬퍼 메서드 재사용
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

    /**
     * 지난 7일간의 분석을 기반으로 맞춤형 루틴을 추천받습니다.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<RoutineRecommendationResponse> getRecommendedRoutines(
            @AuthenticationPrincipal Object principal) {

        try {
            String email = getEmailFromPrincipal(principal);
            RoutineRecommendationResponse response = routineService.getRecommendedRoutines(email);
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }
}