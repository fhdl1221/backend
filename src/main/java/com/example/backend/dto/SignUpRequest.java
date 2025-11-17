package com.example.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SignUpRequest {
    // User Info (Signup Step1)
    private String email;
    private String password;

    // Onboarding Survey
    private String industry;
    private String careerYears;

    // Survey Info
    private List<String> surveyAnswers;

    // Preference Info
    private Map<String, Boolean> preferences;
    private boolean allowNotification;
}
