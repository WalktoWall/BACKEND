package com.walktowall.backend.visitcard.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

import java.time.LocalDateTime;

@Getter
@Builder
public class VisitCardResponse {
    private Integer visitCardId;

    private Integer userId;
    private String userName;

    private Integer gender; // [추가] 성별 필드

    private Integer storeId;
    private String storeName;

    private Integer findProductCategory;
    private Integer moodCategory;
    private String purposeText;
    private LocalDateTime visitTime;
    private Integer supportStatus;

    private String aiMood;
    private List<String> recommendedRoute;
    private LocalDateTime createdAt;
}