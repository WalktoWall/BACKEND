package com.walktowall.backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ReadBestProductResponse {
    private String message;
    private List<BestProduct> bestProductList;

    @Getter
    @Builder
    @AllArgsConstructor
    static public class BestProduct {
        private Integer productId;
        private String productName;
    }
}
