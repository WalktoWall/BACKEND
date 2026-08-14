package com.walktowall.backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductDetailResponse {
    private String message;
    private Integer productId;
    private String productName;
    private String productImg;
}
