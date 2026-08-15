package com.walktowall.backend.visitcard.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class RouteProductResponse {
    private Integer visitCardId;
    private List<ZoneRecommendation> recommendedRoutes;

    @Getter
    @Builder
    public static class ZoneRecommendation {
        private String zone;
        private String description;
        private List<ProductDto> productList;
    }

    @Getter
    @Builder
    public static class ProductDto {
        private Long productId;
        private String productName;
        private String productDetail;
        private String productImg;
        private String location;
        private Integer stock;
    }
}