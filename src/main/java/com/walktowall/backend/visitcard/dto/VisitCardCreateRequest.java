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

    @NotNull(message = "찾는 상품 카테고리는 필수입니다.")
    private Integer findProductCategory;

    @NotNull(message = "무드 카테고리는 필수입니다.")
    private Integer moodCategory;

    private String purposeText;

    private LocalDateTime visitTime;

    @NotNull(message = "직원 응대 상태는 필수입니다.")
    private Integer supportStatus;
}
