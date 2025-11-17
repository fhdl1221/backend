package com.example.backend.dto;

import lombok.Data;
import java.util.Map;

@Data
public class UserProfileUpdateRequest {
    // User 엔티티에서 수정할 필드
    private String industry;
    private String careerYears;

    // UserPreference 엔티티에서 수정할 필드
    private Map<String, Boolean> preferences;
    private boolean allowNotification;
}