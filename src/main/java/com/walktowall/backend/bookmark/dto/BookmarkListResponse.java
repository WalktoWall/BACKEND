package com.walktowall.backend.bookmark.dto;

import com.walktowall.backend.product.entity.ProductEntity;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkListResponse {
    private String message;
    private List<Product> productList;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Product {
        private Integer productId;
        private String productName;
    }
}
