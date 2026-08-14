package com.walktowall.backend.product.controller;

import com.walktowall.backend.product.dto.ProductDetailResponse;
import com.walktowall.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> productDetailResponseResponseEntity
            (@PathVariable Integer productId) {
        ProductDetailResponse response = productService.getProductDetail(productId);

        return ResponseEntity.ok(response);
    }
}
