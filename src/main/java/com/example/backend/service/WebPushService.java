package com.example.backend.service;

import com.example.backend.dto.WebPushSubscriptionDto;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import java.security.Security;

@Service
public class WebPushService {

    private static final Logger logger = LoggerFactory.getLogger(WebPushService.class);

    @Value("${vapid.public.key}")
    private String vapidPublicKey;
    @Value("${vapid.private.key}")
    private String vapidPrivateKey;
    @Value("${vapid.subject}") // 예: mailto:admin@softday.com
    private String vapidSubject;

    private final UserRepository userRepository;
    private PushService pushService;

    public WebPushService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct // 서비스 시작 시 VAPID 키로 PushService 초기화
    private void init() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        this.pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
    }

    // [구독 저장] React가 보낸 구독 정보를 User DB에 저장
    @Transactional
    public void saveSubscription(String email, WebPushSubscriptionDto subDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setWebPushEndpoint(subDto.getEndpoint());
        user.setWebPushP256dh(subDto.getKeys().getP256dh());
        user.setWebPushAuth(subDto.getKeys().getAuth());

        userRepository.save(user);
        logger.info("WebPush 구독 정보 저장 완료: {}", email);
    }

    // [알림 발송] 스케줄러가 이 메서드를 호출
    public void sendNotification(User user, String payloadJson) {
        if (user.getWebPushEndpoint() == null) {
            logger.warn("푸시 알림 실패: 유저 {}의 구독 정보가 없습니다.", user.getEmail());
            return;
        }

        // DB에 저장된 정보로 Subscription 객체 생성
        Subscription sub = new Subscription(
                user.getWebPushEndpoint(),
                new Subscription.Keys(user.getWebPushP256dh(), user.getWebPushAuth())
        );

        try {
            // PushService를 통해 알림 전송
            pushService.send(new Notification(sub, payloadJson));
            logger.info("푸시 알림 전송 성공: {}", user.getEmail());
        } catch (JoseException | GeneralSecurityException | IOException | ExecutionException | InterruptedException e) {
            logger.error("푸시 알림 전송 실패 (User: {}): {}", user.getEmail(), e.getMessage());
            // TODO: 구독이 만료(410 Gone)되었으면 DB에서 user.setWebPushEndpoint(null) 등으로 삭제 처리 필요
        }
    }

    // VAPID 공개키를 프론트엔드에 전달 (React .env를 사용하지 않을 경우)
    public String getVapidPublicKey() {
        return this.vapidPublicKey;
    }
}