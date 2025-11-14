package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CheckIn 요청 DTO
 * 사용자가 현재 상태를 기록할 때 보내는 데이터
 */
public class CheckInRequest {

    private String userId;                    // 사용자 ID
    private String emotion;                   // 현재 감정 (행복, 불안, 우울, 중립 등)
    private Integer stressLevel;              // 스트레스 레벨 (1~10)
    private Integer energyLevel;              // 에너지 수준 (1~10)
    private String note;                      // 자유 기입 메모
    private LocalDateTime timestamp;          // 체크인 시간

    // 신체 상태 (선택사항)
    private Integer sleepHours;               // 수면 시간 (시간)
    private Boolean exercised;                // 운동 여부
    private String mood;                      // 기분 (good, normal, bad)

    // 생성자
    public CheckInRequest() {}

    public CheckInRequest(
            String userId,
            String emotion,
            Integer stressLevel,
            Integer energyLevel,
            String note
    ) {
        this.userId = userId;
        this.emotion = emotion;
        this.stressLevel = stressLevel;
        this.energyLevel = energyLevel;
        this.note = note;
        this.timestamp = LocalDateTime.now();
    }

    // Getter & Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public Integer getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(Integer stressLevel) {
        // 1~10 범위 검증
        if (stressLevel != null && stressLevel >= 1 && stressLevel <= 10) {
            this.stressLevel = stressLevel;
        }
    }

    public Integer getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(Integer energyLevel) {
        // 1~10 범위 검증
        if (energyLevel != null && energyLevel >= 1 && energyLevel <= 10) {
            this.energyLevel = energyLevel;
        }
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(Integer sleepHours) {
        this.sleepHours = sleepHours;
    }

    public Boolean getExercised() {
        return exercised;
    }

    public void setExercised(Boolean exercised) {
        this.exercised = exercised;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    @Override
    public String toString() {
        return "CheckInRequest{" +
                "userId='" + userId + '\'' +
                ", emotion='" + emotion + '\'' +
                ", stressLevel=" + stressLevel +
                ", energyLevel=" + energyLevel +
                ", note='" + note + '\'' +
                ", timestamp=" + timestamp +
                ", sleepHours=" + sleepHours +
                ", exercised=" + exercised +
                ", mood='" + mood + '\'' +
                '}';
    }
}