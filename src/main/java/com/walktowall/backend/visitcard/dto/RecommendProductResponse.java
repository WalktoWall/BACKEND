package com.walktowall.backend.visitcard.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendProductResponse {

    private List<ProductDto> productList;

    @Getter
    @Builder
    public static class ProductDto {
        private Long productId;
        private String productImg;
        private String productZone;
        private String productName;
        private String productDetail;
    }
}