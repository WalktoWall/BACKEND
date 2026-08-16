package com.walktowall.backend.staff.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffProductResponse {

    private String zone;

    private Long productId;

    private String productName;

    private String productImg;

    private String productDetail;
}