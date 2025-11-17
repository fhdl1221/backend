package com.example.backend.service;

import com.example.backend.dto.UserProfileResponse;
import com.example.backend.dto.UserProfileUpdateRequest;
import com.example.backend.model.User;
import com.example.backend.model.UserPreference;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // @Transactional 내부이므로 user.get... 호출 시 Lazy Loading이 동작합니다.
        return new UserProfileResponse(user, user.getUserPreference(), user.getInitialSurvey());
    }

    @Transactional
    public UserProfileResponse updateUserProfile(String email, UserProfileUpdateRequest request) {
        // 1. 사용자 조회 (영속성 컨텍스트에 포함됨)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. User 엔티티 필드 업데이트 (Dirty Checking)
        user.setIndustry(request.getIndustry());
        user.setCareerYears(request.getCareerYears());

        // 3. UserPreference 엔티티 필드 업데이트 (Dirty Checking)
        UserPreference prefs = user.getUserPreference();
        if (prefs != null) {
            prefs.setNotificationEnable(request.isAllowNotification());

            Map<String, Boolean> prefMap = request.getPreferences();
            if (prefMap != null) {
                prefs.setContentTypeVideo(prefMap.getOrDefault("video", false));
                prefs.setContentTypeAudio(prefMap.getOrDefault("audio", false));
                prefs.setContentTypeText(prefMap.getOrDefault("text", false));
            }
        }
        return new UserProfileResponse(user, user.getUserPreference(), user.getInitialSurvey());
    }
}