package com.walktowall.backend.wallart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreateWallartRequest {
    @Valid // 내부 객체 검증 수행
    @NotNull(message = "visitCard 정보는 필수입니다.")
    private VisitCard visitCard;

    @Getter
    @NoArgsConstructor
    public static class VisitCard {
        private Integer visitCardId;
        private Integer userId;
        private Integer storeId;

        private Integer gender;

        private Integer findProductCategory;
        private Integer moodCategory;
        private String purposeText;
        private LocalDateTime visitTime;

        private String aiMood;
        private List<String> recommendedRoute;
    }
}
