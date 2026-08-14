package com.walktowall.backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProductHistoryResponse {
    private String message;
    private List<Product> productList;

    @Getter
    @Builder
    public static class Product {
        private Integer productId;
        private String productName;
    }
}

