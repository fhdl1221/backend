package com.example.backend.controller;

import com.example.backend.dto.WebPushSubscriptionDto;
import com.example.backend.scheduler.StressCheckScheduler;
import com.example.backend.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final WebPushService webPushService;
    private final StressCheckScheduler stressCheckScheduler;

    // React가 구독에 필요한 VAPID 공개키를 요청하는 API
    @GetMapping("/vapid-public-key")
    public ResponseEntity<Map<String, String>> getVapidPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", webPushService.getVapidPublicKey()));
    }

    // React가 생성한 구독 정보를 DB에 저장하는 API
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @RequestBody WebPushSubscriptionDto subscription,
            Authentication authentication) {

        String email = authentication.getName();
        webPushService.saveSubscription(email, subscription);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * [신규] 푸시 알림을 즉시 테스트하는 API
     */
    @GetMapping("/test-push")
    public ResponseEntity<Map<String, String>> sendTestNotification(Authentication authentication) {
        String email = authentication.getName();
        try {
            // 스케줄러의 단일 사용자 검사 로직을 '테스트 모드(true)'로 즉시 호출
            String resultMessage = stressCheckScheduler.checkSingleUserStress(email, true);

            return ResponseEntity.ok(Map.of(
                    "message", "테스트 알림을 성공적으로 요청했습니다.",
                    "result", resultMessage
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "테스트 알림 실패: " + e.getMessage()));
        }
    }
}