package com.example.backend.dto;

import com.example.backend.model.DailyCheckIn;
import com.example.backend.model.StressCause;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CheckInResponse {
    private Long id;
    private LocalDate checkinDate;
    private Integer stressLevel;
    private String stressEmoji;
    private String memo;
    private List<String> stressCauses;
    private LocalDateTime createdAt;

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static CheckInResponse fromEntity(DailyCheckIn entity) {
        CheckInResponse dto = new CheckInResponse();
        dto.setId(entity.getId());
        dto.setCheckinDate(entity.getCheckinDate());
        dto.setStressLevel(entity.getStressLevel());
        dto.setStressEmoji(entity.getStressEmoji());
        dto.setMemo(entity.getMemo());
        dto.setCreatedAt(entity.getCreatedAt());

        // StressCause 엔티티 리스트를 문자열 리스트로 변환
        if (entity.getStressCauses() != null) {
            dto.setStressCauses(entity.getStressCauses().stream()
                    .map(StressCause::getCauseType)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}