package com.walktowall.backend.staff.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class StaffVisitResponse {

    private Integer visitCardId;

    private Integer userId;

    private String userName;

    private String storeName;

    private LocalDateTime visitTime;

    private String gender;

    private String findProductCategory;

    private String moodCategory;

    private String purposeText;

    private String aiMood;

    private List<String> recommendedRoute;

    private List<StaffProductResponse> startRecommendedProducts;

    private String staffGuidance;
}