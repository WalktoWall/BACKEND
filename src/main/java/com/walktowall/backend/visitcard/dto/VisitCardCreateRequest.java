package com.walktowall.backend.visitcard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

public class VisitCardCreateRequest {
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Integer userId;

    @NotNull(message = "매장 ID는 필수입니다.")
    private Integer storeId;

    private Integer findProductCategory;

    @NotNull(message = "무드 카테고리는 필수입니다.")
    private Integer moodCategory;

    @NotNull(message = "방문 목적은 필수입니다.")
    private String purposeText;

    @NotNull(message = "방문 예정 시간은 필수입니다.")
    private LocalDateTime visitTime;

    @NotNull(message = "직원 응대 상태는 필수입니다.")
    private Integer supportStatus;

    // [추가] 성별 필드
    @NotNull(message = "성별은 필수입니다.")
    private Integer gender;
}
