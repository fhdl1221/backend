package com.example.backend.dto;

import com.example.backend.model.InitialSurvey;
import com.example.backend.model.User;
import com.example.backend.model.UserPreference;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class UserProfileResponse {
    private String email;
    private String industry;
    private String careerYears;
    private List<String> surveyAnswers;
    private Map<String, Boolean> preferences;
    private boolean allowNotification;

    // 참고: 보안상 비밀번호(password)는 절대 반환하지 않습니다.

    /**
     * 여러 엔티티를 DTO로 변환하는 생성자
     */
    public UserProfileResponse(User user, UserPreference prefs, InitialSurvey survey) {
        this.email = user.getEmail();
        this.industry = user.getIndustry();
        this.careerYears = user.getCareerYears();

        // UserPreference 정보 매핑
        if (prefs != null) {
            this.allowNotification = prefs.isNotificationEnable();
            Map<String, Boolean> prefMap = new HashMap<>();
            prefMap.put("video", prefs.isContentTypeVideo());
            prefMap.put("text", prefs.isContentTypeText());
            prefMap.put("audio", prefs.isContentTypeAudio());
            this.preferences = prefMap;
        } else {
            // 기본값
            this.allowNotification = false;
            this.preferences = new HashMap<>();
        }

        // InitialSurvey 정보 매핑 (질문 1~5를 리스트로)
        if (survey != null) {
            this.surveyAnswers = Arrays.asList(
                    survey.getQuestion1(),
                    survey.getQuestion2(),
                    survey.getQuestion3(),
                    survey.getQuestion4(),
                    survey.getQuestion5()
            );
        } else {
            // 기본값
            this.surveyAnswers = Arrays.asList(null, null, null, null, null);
        }
    }
}