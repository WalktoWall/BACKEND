package com.walktowall.backend.wallart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreateWallartRequest {
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
        private Integer supportStatus;

        private String aiMood;
        private List<String> recommendedRoute;
    }
}
